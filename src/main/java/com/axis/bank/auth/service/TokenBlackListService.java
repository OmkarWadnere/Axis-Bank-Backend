package com.axis.bank.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.axis.bank.utility.Constants.BLACKLIST_PREFIX;

@Service
public class TokenBlackListService {

    private final StringRedisTemplate redisTemplate;

    public TokenBlackListService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isBlackListed(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
