package com.axis.bank.service;

import com.axis.bank.auth.security.JwtProvider;
import com.axis.bank.entity.TemporarySignUpUser;
import com.axis.bank.entity.User;
import com.axis.bank.exception.AxisBankException;
import com.axis.bank.models.dto.LoginRequest;
import com.axis.bank.models.dto.LoginResponse;
import com.axis.bank.models.dto.LogoutResponse;
import com.axis.bank.models.dto.OtpRequest;
import com.axis.bank.models.dto.OtpResponse;
import com.axis.bank.models.dto.SignUpRequest;
import com.axis.bank.models.dto.SignUpResponse;
import com.axis.bank.models.dto.VerifyOtpRequest;
import com.axis.bank.models.dto.VerifyOtpResponse;
import com.axis.bank.models.emum.MathematicalOperation;
import com.axis.bank.models.emum.Role;
import com.axis.bank.repository.TemporarySignUpUserRepository;
import com.axis.bank.repository.UserRepository;
import com.axis.bank.service.helper.OtpHelper;
import com.axis.bank.utility.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.axis.bank.utility.Constants.BLACKLIST_PREFIX;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BloomFilterService bloomFilterService;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final OtpHelper otpHelper;
    private final TemporarySignUpUserRepository temporarySignUpUserRepository;
    private final EmailService emailService;

    @Value("${user.signupTime}")
    private Long singUpTime;

    @Transactional
    public OtpResponse generateOtp(OtpRequest otpRequest) throws AxisBankException {
        Optional<TemporarySignUpUser> temporarySignUpUserRepositoryOptional = temporarySignUpUserRepository.findByEmailId(otpRequest.getEmailId());
        Optional<User> userOptional = userRepository.findByEmailId(otpRequest.getEmailId());
        if (userOptional.isPresent()) {
            throw new AxisBankException("User Already Exists!!!", HttpStatus.BAD_REQUEST);
        }
        TemporarySignUpUser temporarySignUpUser = new TemporarySignUpUser();
        if (temporarySignUpUserRepositoryOptional.isPresent()) {
            temporarySignUpUser = temporarySignUpUserRepositoryOptional.get();
            if (Boolean.TRUE.equals(temporarySignUpUser.getIsUserLocked())) {
                if (LocalDateTime.now().isBefore(temporarySignUpUser.getUserLockedTime().plusMinutes(5))) {
                    Duration duration = Duration.between(LocalDateTime.now(), temporarySignUpUser.getUserLockedTime().plusMinutes(5));
                    long minutes = duration.getSeconds() / 60;
                    long seconds = duration.getSeconds() % 60;
                    throw new AxisBankException("User is Locked please try after " + minutes + " minutes and " + seconds + " seconds.", HttpStatus.BAD_REQUEST);
                }
                if (LocalDateTime.now().isAfter(temporarySignUpUser.getUserLockedTime().plusMinutes(5))) {
                    temporarySignUpUser.setIsUserLocked(Boolean.FALSE);
                    temporarySignUpUser.setUserLockedTime(null);
                    temporarySignUpUser.setOtpAttempts(0L);
                    temporarySignUpUserRepository.save(temporarySignUpUser);
                }
            }
        } else {
            temporarySignUpUser.setIsUserLocked(Boolean.FALSE);
        }
        // Cooldown: read lastSent timestamp (in millis)
        String lastOtpGenerationTimeStr = redisTemplate.opsForValue().get(otpHelper.lastOtpGenerationTime(otpRequest.getEmailId()));
        Long lastOtpGenerationTime = null;
        if (StringUtils.isNotEmpty(lastOtpGenerationTimeStr)) {
            lastOtpGenerationTime = Long.parseLong(lastOtpGenerationTimeStr);
        }
        if (StringUtils.isEmpty(lastOtpGenerationTimeStr) && StringUtils.isNotEmpty(temporarySignUpUser.getEmailId())) {
            lastOtpGenerationTime = temporarySignUpUser.getLastOtpGenerationTime();
        }

        // Request count per hour (increment + set TTL only when new)
        Long requests = redisTemplate.opsForValue().increment(otpHelper.requestCountForSignUp(otpRequest.getEmailId()));
        if (null != requests && requests == 1) {
            redisTemplate.expire(otpHelper.requestCountForSignUp(otpRequest.getEmailId()), Duration.ofMinutes(5));
        }
        if (null != requests && requests > otpHelper.getMaxRequestPerHour()) {
            throw new AxisBankException("Maximum OTP Requests reached. Try after sometime", HttpStatus.TOO_MANY_REQUESTS);
        }

        // Cooldown: read lastSent timestamp (in millis)
        if (null != lastOtpGenerationTime) {
            long now = System.currentTimeMillis();
            long diff = now - lastOtpGenerationTime;
            if (diff < otpHelper.getCoolDownSeconds() * 1000) {
                long waitTime = otpHelper.getCoolDownSeconds() - (diff / 1000);
                throw new AxisBankException("Resend OTP after " + waitTime + "s", HttpStatus.TOO_MANY_REQUESTS);
            }
        }

        // Generate OTP and cache it
        String otp = otpHelper.generateOtp();
        String hashedOtp = otpHelper.hmac(otpRequest.getEmailId() + "|" + otp);
        redisTemplate.opsForValue().set(otpHelper.otpKey(otpRequest.getEmailId()), hashedOtp, Duration.ofSeconds(otpHelper.getTtlSeconds()));
        redisTemplate.opsForValue().set(otpHelper.lastOtpGenerationTime(otpRequest.getEmailId()), String.valueOf(System.currentTimeMillis()), Duration.ofSeconds(otpHelper.getCoolDownSeconds()));
        redisTemplate.delete(otpHelper.attemptsKey(otpRequest.getEmailId()));

        temporarySignUpUser.setEmailId(otpRequest.getEmailId());
        temporarySignUpUser.setOtp(hashedOtp);
        temporarySignUpUser.setLastOtpGenerationTime(System.currentTimeMillis());
        temporarySignUpUser.setIsOtpUsed(Boolean.FALSE);
        temporarySignUpUser.setOtpAttempts(0L);
        temporarySignUpUserRepository.save(temporarySignUpUser);
        String otpMailBody = "Dear User,\n"
                + "\n\t Your OTP to register in Axis Bank application is: " + otp + "."
                + "\n\n Thanks & Regards,\n"
                + " Axis Bank";
        emailService.sendMail(otpRequest.getEmailId(), otpMailBody, "OTP for Signup User Request");
        return OtpResponse.builder().message("OTP sent successfully!!!").build();
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest verifyOtpRequest) throws AxisBankException {
        Optional<TemporarySignUpUser> temporarySignUpUserOptional = temporarySignUpUserRepository.findByEmailId(verifyOtpRequest.getEmailId());
        if (temporarySignUpUserOptional.isEmpty()) {
            throw new AxisBankException("User does not exists", HttpStatus.NOT_FOUND);
        }

        TemporarySignUpUser temporarySignUpUser = temporarySignUpUserOptional.get();
        String otpKey = redisTemplate.opsForValue().get(otpHelper.otpKey(temporarySignUpUser.getEmailId()));
        if (StringUtils.isEmpty(otpKey)) {
            otpKey = temporarySignUpUser.getOtp();
        }

        if (Boolean.TRUE.equals(temporarySignUpUser.getIsUserLocked())) {
            if (LocalDateTime.now().isBefore(temporarySignUpUser.getUserLockedTime().plusMinutes(5))) {
                Duration duration = Duration.between(LocalDateTime.now(), temporarySignUpUser.getUserLockedTime().plusMinutes(5));
                long minutes = duration.getSeconds() / 60;
                long seconds = duration.getSeconds() % 60;
                throw new AxisBankException("User is Locked please try after " + minutes + " minutes and " + seconds + " seconds.", HttpStatus.BAD_REQUEST);
            }
            if (LocalDateTime.now().isAfter(temporarySignUpUser.getUserLockedTime().plusMinutes(5))) {
                temporarySignUpUser.setIsUserLocked(Boolean.FALSE);
                temporarySignUpUser.setUserLockedTime(null);
                temporarySignUpUser.setOtpAttempts(0L);
                temporarySignUpUserRepository.save(temporarySignUpUser);
            }
        }

        String requestOtpKey = otpHelper.hmac(verifyOtpRequest.getEmailId() + "|" + verifyOtpRequest.getOtp());
        long currentTime = System.currentTimeMillis();
        long difference = currentTime - temporarySignUpUser.getLastOtpGenerationTime();

        if (difference > otpHelper.getTtlSeconds() * 1000) {
            throw new AxisBankException("OTP expired!!!", HttpStatus.BAD_REQUEST);
        }

        if (Boolean.TRUE.equals(temporarySignUpUser.getIsOtpUsed())) {
            throw new AxisBankException("OTP already used please generate new OTP.", HttpStatus.BAD_REQUEST);
        }

        if (requestOtpKey.equals(otpKey)) {
            temporarySignUpUser.setIsOtpUsed(Boolean.TRUE);
            temporarySignUpUserRepository.save(temporarySignUpUser);
            String key = getUserSignUpVerificationKey(temporarySignUpUser.getEmailId());
            redisTemplate.opsForValue().set(key, Constants.VERIFIED);
            redisTemplate.expire(key, Duration.ofMinutes(15));
            return VerifyOtpResponse.builder().message("User verified successfully!!!!").build();
        } else {
            if (temporarySignUpUser.getOtpAttempts() > otpHelper.getMaxVerifyAttempts()) {
                temporarySignUpUser.setIsUserLocked(Boolean.TRUE);
                temporarySignUpUser.setUserLockedTime(LocalDateTime.now());
                temporarySignUpUserRepository.save(temporarySignUpUser);
                throw new AxisBankException("You have reached maximum limit, user is locked for 5 minutes", HttpStatus.BAD_REQUEST);
            }
            temporarySignUpUser.setOtpAttempts(temporarySignUpUser.getOtpAttempts() + 1);
            temporarySignUpUserRepository.save(temporarySignUpUser);
            throw new AxisBankException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }
    }


    @Transactional
    public SignUpResponse signUpUser(SignUpRequest signUpRequest) throws AxisBankException {
        if (Constants.VERIFIED.equals(redisTemplate.opsForValue().get(getUserSignUpVerificationKey(signUpRequest.getEmailId())))) {
            String emailIdKey = buildEmailIdKey(signUpRequest.getEmailId());
            String mobileNumberKey = buildMobileNumberKey(signUpRequest.getMobileNumber());
            if (bloomFilterService.mightExist(emailIdKey) || bloomFilterService.mightExist(mobileNumberKey)
                    || userRepository.existsByEmailIdOrMobileNumber(signUpRequest.getEmailId(), signUpRequest.getMobileNumber())) {
                log.error("User Already Exists");
                throw new AxisBankException("User Already Exists", HttpStatus.BAD_REQUEST);
            }
            saveUserInDb(signUpRequest);
            bloomFilterService.add(emailIdKey, mobileNumberKey);
            evictExistenceCache(signUpRequest);
            return SignUpResponse.builder().message("User Added Successfully!!").build();
        } else {
            throw new AxisBankException("Please verify user first!!!", HttpStatus.UNAUTHORIZED);
        }
    }

    @Transactional
    public LoginResponse loginUser(LoginRequest loginRequest) throws AxisBankException {
        User user = verifyUser(loginRequest);
        verifyUserInvalidPasswordCountAndDisableAccount(user);
        verifyUserPassword(user, loginRequest.getPassword());
        updateUserInvalidPasswordCount(user, MathematicalOperation.NEUTRALIZE);
        return loginResponseBuilder(user);
    }

    public LogoutResponse logoutUser(HttpServletRequest request) throws AxisBankException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtProvider.validateToken(token)) {
                long remainingValidity = jwtProvider.getRemainingValidity(token);
                if (remainingValidity > 0) {
                    redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true", remainingValidity, TimeUnit.MILLISECONDS);
                }
                return LogoutResponse.builder().message("Logout successfully!!!").build();
            } else {
                throw new AxisBankException("Invalid Token", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new AxisBankException("Missing token in header", HttpStatus.UNAUTHORIZED);
        }
    }

    private User verifyUser(LoginRequest loginRequest) throws AxisBankException {
        Optional<User> userByMobileNumber = userRepository.findByMobileNumber(loginRequest.getUserName());
        Optional<User> userByEmailId = userRepository.findByEmailId(loginRequest.getUserName());
        if (userByMobileNumber.isEmpty() && userByEmailId.isEmpty()) {
            throw new AxisBankException("User does not exists", HttpStatus.NOT_FOUND);
        }
        return userByMobileNumber.orElseGet(userByEmailId::get);
    }


    private void saveUserInDb(SignUpRequest signUpRequest) {
        User user = User.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .birthDate(signUpRequest.getBirthDate())
                .emailId(signUpRequest.getEmailId())
                .mobileNumber(signUpRequest.getMobileNumber())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(Collections.singleton(Role.valueOf(signUpRequest.getRole())))
                .invalidPasswordCounter(0)
                .enabled(true)
                .locked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        userRepository.save(user);
    }

    private String buildEmailIdKey(String emailId) {
        return (emailId == null ? "" : emailId.trim().toLowerCase());
    }

    private String buildMobileNumberKey(String mobileNumber) {
        return (mobileNumber == null ? "" : mobileNumber.trim());
    }

    private void evictExistenceCache(SignUpRequest req) {
        String emailIdCacheKey = req.getEmailId();
        String mobileNumberCacheKey = req.getMobileNumber();
        Cache cache = cacheManager.getCache("userExists");
        if (cache != null) cache.evict(emailIdCacheKey);
        if (cache != null) cache.evict(mobileNumberCacheKey);
    }

    private void verifyUserPassword(User user, String requestPassword) throws AxisBankException {
        if (!passwordEncoder.matches(requestPassword, user.getPassword())) {
            updateUserInvalidPasswordCount(user, MathematicalOperation.ADDITION);
            throw new AxisBankException("Invalid Password", HttpStatus.BAD_REQUEST);
        }
    }

    private void updateUserInvalidPasswordCount(User user, MathematicalOperation operation) {
        if (operation.getValue().equals(MathematicalOperation.ADDITION.getValue())) {
            user.setInvalidPasswordCounter(user.getInvalidPasswordCounter() + 1);
        } else if (operation.getValue().equals(MathematicalOperation.NEUTRALIZE.getValue())) {
            user.setInvalidPasswordCounter(0);
        }
        userRepository.save(user);
    }

    private void verifyUserInvalidPasswordCountAndDisableAccount(User user) throws AxisBankException {
        if (user.getInvalidPasswordCounter() > 3) {
            user.setEnabled(false);
            user.setLocked(true);
            user.setLockedTime(LocalDateTime.now());
            userRepository.save(user);
            throw new AxisBankException("Reached maximum incorrect password count, Please reset password", HttpStatus.BAD_REQUEST);
        }
    }

    private LoginResponse loginResponseBuilder(User user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);
        return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
                .expiresIn(jwtProvider.getExpiryDate(accessToken).getTime() / 1000).build();
    }

    private String getUserSignUpVerificationKey(String emailId) {
        return "user:signUp:" + emailId;
    }
}
