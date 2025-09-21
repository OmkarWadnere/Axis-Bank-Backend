package com.axis.bank.configuration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        // 👇 define cache name "userExists"
        config.put("userExists", new CacheConfig(60_000, 30_000));
        // TTL = 60s, maxIdleTime = 30s (tune as needed)

        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
