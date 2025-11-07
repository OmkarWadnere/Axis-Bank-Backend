package com.axis.bank.service.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class OtpHelper {

    private final SecureRandom secureRandom;

    @Value("${otp.ttlSeconds}")
    private Long ttlSeconds;

    @Value("${otp.cooldownSeconds}")
    private Long coolDownSeconds;

    @Value("${otp.maxVerifyAttempts}")
    private Long maxVerifyAttempts;

    @Value("${otp.maxRequestPerHour}")
    private Long maxRequestPerHour;

    @Value("${otp.hmacSecret}")
    private String hmacSecret;

    // Generate cryptographically secure 6-digit OTP
    public String generateOtp() {
        int otp = secureRandom.nextInt(900_000) + 100_000;
        return String.valueOf(otp);
    }

    // HMAC-SHA256
    public String hmac(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] raw = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC", e);
        }
    }

    // Constant-time compare
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public String otpKey(Long userId) {
        return "otp:" + userId;
    }

    public String otpKey(String emailId) {
        return "otp:" + emailId;
    }

    public String attemptsKey(Long userId) {
        return "otp:attempts:" + userId;
    }

    public String attemptsKey(String emailId) {
        return "otp:attempts:" + emailId;
    }

    public String lastSentKey(Long userId) {
        return "otp:lastSent:" + userId;
    }

    public String lastOtpGenerationTime(String emailId) {
        return "otp:lastOtpGenerationTime:" + emailId;
    }

    public String requestCountKey(Long userId) {
        return "otp:requestCountKey:" + userId;
    }

    public String requestCountForSignUp(String emailId) {
        return "otp:requestCountKey:" + emailId;
    }


    public Long getCoolDownSeconds() {
        return coolDownSeconds;
    }

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public Long getMaxVerifyAttempts() {
        return maxVerifyAttempts;
    }

    public Long getMaxRequestPerHour() {
        return maxRequestPerHour;
    }

    public String getHmacSecret() {
        return hmacSecret;
    }
}
