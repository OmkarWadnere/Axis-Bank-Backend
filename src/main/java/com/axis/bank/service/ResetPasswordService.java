package com.axis.bank.service;

import com.axis.bank.entity.User;
import com.axis.bank.entity.UserOtp;
import com.axis.bank.exception.AxisBankException;
import com.axis.bank.models.dto.OtpRequest;
import com.axis.bank.models.dto.OtpResponse;
import com.axis.bank.models.dto.ResetPasswordRequest;
import com.axis.bank.models.dto.ResetPasswordResponse;
import com.axis.bank.models.dto.VerifyOtpRequest;
import com.axis.bank.models.dto.VerifyOtpResponse;
import com.axis.bank.repository.UserOtpRepository;
import com.axis.bank.repository.UserRepository;
import com.axis.bank.service.helper.OtpHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {

    private final StringRedisTemplate redisTemplate;
    private final UserOtpRepository userOtpRepository;
    private final UserRepository userRepository;
    private final OtpHelper otpHelper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    /**
     * Generate OTP, store HMAC in Redis (TTL) and DB (audit) and send OTP asynchronously.
     * Returns a generic response (no user enumeration).
     */
    @Transactional
    public OtpResponse generateOtpAndSendOtp(OtpRequest otpRequest) throws AxisBankException, NoSuchAlgorithmException {
        // Verify user register with system or not
        Optional<User> userOptional = userRepository.findByEmailId(otpRequest.getEmailId());
        if (userOptional.isEmpty()) {
            throw new AxisBankException("User doesn't exists", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        Long userId = userOptional.get().getUserId();
        Optional<UserOtp> userOtpOptional = userOtpRepository.findByUserUserId(userId);
        UserOtp userOtp = new UserOtp();
        if (userOtpOptional.isPresent()) {
            userOtp = userOtpOptional.get();
        }
        // Cooldown: read lastSent timestamp (in millis)
        String lastOtpGenerationTimeStr = redisTemplate.opsForValue().get(otpHelper.lastSentKey(userId));
        Long lastOtpGenerationTime = null;
        if (StringUtils.isNotEmpty(lastOtpGenerationTimeStr)) {
            lastOtpGenerationTime = Long.parseLong(lastOtpGenerationTimeStr);
        }
        if (StringUtils.isEmpty(lastOtpGenerationTimeStr)) {
            if (userOtpOptional.isPresent()) {
                lastOtpGenerationTime = userOtp.getCreatedAt();
            }
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
        // Generate OTP & HMAC (tie to userId + purpose to avoid reuse across contexts)
        // Generate OTP and cache it
        String otp = otpHelper.generateOtp();
        String hashedOtp = otpHelper.hmac(otpRequest.getEmailId() + "|" + otp);
        redisTemplate.opsForValue().set(otpHelper.otpKey(otpRequest.getEmailId()), hashedOtp, Duration.ofSeconds(otpHelper.getTtlSeconds()));
        redisTemplate.opsForValue().set(otpHelper.lastOtpGenerationTime(otpRequest.getEmailId()), String.valueOf(System.currentTimeMillis()), Duration.ofSeconds(otpHelper.getCoolDownSeconds()));
        redisTemplate.delete(otpHelper.attemptsKey(otpRequest.getEmailId()));
        userOtp.setOtp(hashedOtp);
        userOtp.setUser(user);
        userOtp.setIsOtpUsed(Boolean.FALSE);
        userOtp.setOtpAttempts(0L);
        userOtp.setCreatedAt(System.currentTimeMillis());
        userOtpRepository.save(userOtp);
        String otpMailBody = "Dear " + user.getFirstName() + " " + user.getLastName() + ","
                + "\n\n\t Your OTP for forgot password request is: " + otp + "."
                + "\n\n Thanks & Regards,"
                + "\n Axis Bank";
        emailService.sendMail(otpRequest.getEmailId(), otpMailBody, "OTP for Forgot Password Request");
        return OtpResponse.builder().message("OTP sent successfully!!!").build();
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest verifyOtpRequest) throws AxisBankException {
        Optional<User> userOptional = userRepository.findByEmailId(verifyOtpRequest.getEmailId());
        if (userOptional.isEmpty()) {
            throw new AxisBankException("User doesn't exists", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        Long userId = user.getUserId();
        String otpKey = redisTemplate.opsForValue().get(otpHelper.otpKey(user.getEmailId()));

        if (Boolean.FALSE.equals(user.getEnabled()) && Boolean.TRUE.equals(user.getLocked())) {
            if (LocalDateTime.now().isBefore(user.getLockedTime().plusMinutes(5))) {
                Duration duration = Duration.between(LocalDateTime.now(), user.getLockedTime().plusMinutes(5));
                long minutes = duration.getSeconds() / 60;
                long seconds = duration.getSeconds() % 60;
                throw new AxisBankException("User is Locked please try after " + minutes + " minutes and " + seconds + " seconds.", HttpStatus.BAD_REQUEST);
            }
            if (LocalDateTime.now().isAfter(user.getLockedTime().plusMinutes(5))) {
                user.setEnabled(true);
                user.setLocked(false);
                user.setLockedTime(null);
                userRepository.save(user);
            }
        }

        Optional<UserOtp> userOtpOptional = userOtpRepository.findByUserUserId(userId);
        UserOtp userOtp = new UserOtp();
        if (userOtpOptional.isPresent()) {
            userOtp = userOtpOptional.get();
        }
        if (StringUtils.isEmpty(otpKey)) {
            otpKey = userOtp.getOtp();
        }

        String requestOtpKey = otpHelper.hmac(verifyOtpRequest.getEmailId() + "|" + verifyOtpRequest.getOtp());
        long currentTime = System.currentTimeMillis();
        long difference = currentTime - userOtp.getCreatedAt();

        if (difference > otpHelper.getTtlSeconds() * 1000) {
            throw new AxisBankException("OTP expired!!!", HttpStatus.BAD_REQUEST);
        }

        if (Boolean.TRUE.equals(userOtp.getIsOtpUsed())) {
            throw new AxisBankException("OTP already used please generate new OTP.", HttpStatus.BAD_REQUEST);
        }

        if (requestOtpKey.equals(otpKey)) {
            userOtp.setIsOtpUsed(Boolean.TRUE);
            userOtpRepository.save(userOtp);
            user.setIsUserEligibleForPasswordReset(Boolean.TRUE);
            user.setEligibleTime(LocalDateTime.now());
            userRepository.save(user);
            return VerifyOtpResponse.builder().message("OTP verified!!!").build();
        } else {
            if (userOtp.getOtpAttempts() > otpHelper.getMaxVerifyAttempts()) {
                user.setEnabled(false);
                user.setLocked(true);
                user.setLockedTime(LocalDateTime.now());
                userRepository.save(user);
                throw new AxisBankException("You have reached maximum limit, user is locked for 5 minutes", HttpStatus.BAD_REQUEST);
            }
            userOtp.setOtpAttempts(userOtp.getOtpAttempts() + 1);
            userOtpRepository.save(userOtp);
            throw new AxisBankException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }
    }

    public ResetPasswordResponse resetPassword(ResetPasswordRequest resetPasswordRequest) throws AxisBankException {
        Optional<User> userOptional = userRepository.findByEmailId(resetPasswordRequest.getEmailId());
        if (userOptional.isEmpty()) {
            throw new AxisBankException("User doesn't exists", HttpStatus.BAD_REQUEST);
        }
        User user = userOptional.get();
        if (null == user.getEligibleTime() || LocalDateTime.now().isAfter(user.getEligibleTime().plusMinutes(5))) {
            user.setIsUserEligibleForPasswordReset(Boolean.FALSE);
            user.setEligibleTime(null);
            userRepository.save(user);
            throw new AxisBankException("Please generate OTP and verify it first", HttpStatus.BAD_REQUEST);
        }
        user.setInvalidPasswordCounter(0);
        user.setEnabled(Boolean.TRUE);
        user.setLocked(Boolean.FALSE);
        user.setIsUserEligibleForPasswordReset(Boolean.FALSE);
        user.setEligibleTime(null);
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);
        return ResetPasswordResponse.builder().message("Password reset successfully!!!").build();
    }

}
