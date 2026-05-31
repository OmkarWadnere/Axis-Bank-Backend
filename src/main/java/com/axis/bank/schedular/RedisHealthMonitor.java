package com.axis.bank.schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthMonitor {

    private final StringRedisTemplate redisTemplate;

    private volatile boolean redisUp = true;

    @Scheduled(fixedDelay = 10000)
    public void checkRedisHealth() {

        try {
            assert redisTemplate.getConnectionFactory() != null;
            try (RedisConnection connection =
                         redisTemplate.getConnectionFactory().getConnection()) {

                String response = connection.ping();

                if ("PONG".equalsIgnoreCase(response)) {

                    if (!redisUp) {
                        log.debug("✅ Redis is back online. Connection restored.");
                        redisUp = true;
                    }
                }

            }
        } catch (Exception ex) {

            if (redisUp) {
                log.error("❌ Redis is DOWN. Lettuce will automatically try to reconnect.");
                redisUp = false;
            } else {
                log.warn("🔄 Redis still unavailable. Auto-reconnect is in progress...");
            }
        }
    }
}