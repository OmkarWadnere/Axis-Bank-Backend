package com.axis.bank.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlackListService {

    private static final String PREFIX = "blacklist:";
    private final StringRedisTemplate redisTemplate;

    public TokenBlackListService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blackList(String token, Duration ttl) {
        redisTemplate.opsForValue().set(PREFIX + token, "true", ttl);
    }

    public boolean isBlackListed(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}
