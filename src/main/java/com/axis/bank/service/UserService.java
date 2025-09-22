package com.axis.bank.service;

import com.axis.bank.entity.User;
import com.axis.bank.exception.AxisBankException;
import com.axis.bank.models.dto.SignUpRequest;
import com.axis.bank.models.dto.SignUpResponse;
import com.axis.bank.models.emum.Role;
import com.axis.bank.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BloomFilterService bloomFilterService;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    @Transactional
    public SignUpResponse signUpUser(SignUpRequest signUpRequest) throws AxisBankException {
        String emailIdKey = buildEmailIdKey(signUpRequest.getEmailId());
        String mobileNumberKey = buildMobileNumberKey(signUpRequest.getMobileNumber());

        if ((bloomFilterService.mightExist(emailIdKey) || bloomFilterService.mightExist(mobileNumberKey))
                && userRepository.existsByEmailIdOrMobileNumber(signUpRequest.getEmailId(), signUpRequest.getMobileNumber())) {
            log.error("User Already Exists");
            throw new AxisBankException("User Already Exists", HttpStatus.BAD_REQUEST);
        }
        User user = User.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .birthDate(signUpRequest.getBirthDate())
                .emailId(signUpRequest.getEmailId())
                .mobileNumber(signUpRequest.getMobileNumber())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(Collections.singleton(Role.valueOf(signUpRequest.getRole())))
                .enabled(true)
                .locked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        userRepository.save(user);
        bloomFilterService.add(emailIdKey, mobileNumberKey);
        evictExistenceCache(signUpRequest);
        return SignUpResponse.builder().message("User Added Successfully!!").build();
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
}
