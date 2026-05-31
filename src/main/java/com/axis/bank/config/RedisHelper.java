package com.axis.bank.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisHelper {

    private final StringRedisTemplate redisTemplate;
    private final RedisTtlProperties ttlProperties;

    public void setWithTtl(String key, String value, Duration ttl) {
        if (ttl == null) {
            // Explicitly avoid silent no-TTL writes; prefer callers to pass ttl. Fall back to no-ttl only if necessary.
            redisTemplate.opsForValue().set(key, value, ttlProperties.globalDuration());
        } else {
            redisTemplate.opsForValue().set(key, value, ttl);
        }
    }

    public void setSignupVerification(String key) {
        setWithTtl(key, "Verified", ttlProperties.signupVerificationDuration());
    }

    public void setOtpForEmail(String key, String hashedOtp) {
        setWithTtl(key, hashedOtp, ttlProperties.otpDuration());
    }

    public void setLastOtpGenerationTime(String key) {
        setWithTtl(key, String.valueOf(System.currentTimeMillis()), ttlProperties.cooldownDuration());
    }

    public Long incrementAndExpireIfFirst(String key, Duration ttl) {
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1 && ttl != null) {
            redisTemplate.expire(key, ttl);
        }
        return value;
    }

    // Convenience: increment using configured request-count TTL
    public Long incrementRequestCountForSignUp(String key) {
        return incrementAndExpireIfFirst(key, ttlProperties.requestCountDuration());
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}

