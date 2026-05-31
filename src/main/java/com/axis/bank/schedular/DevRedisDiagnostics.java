package com.axis.bank.schedular;

import com.axis.bank.config.RedisHelper;
import com.axis.bank.config.RedisTtlProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevRedisDiagnostics {

    private final RedisTtlProperties redisTtlProperties;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(initialDelay = 10_000, fixedDelay = 30 * 60 * 1000)
    public void scanAndFixKeysWithoutTtl() {

        try {

            Set<String> keys = redisTemplate.keys("*");

            if (keys == null || keys.isEmpty()) {
                return;
            }

            int fixedCount = 0;

            for (String key : keys) {

                Long ttl = redisTemplate.getExpire(key);

                // -1 => Key exists but has no expiration
                if (ttl != null && ttl == -1) {

                    Boolean success = redisTemplate.expire(
                            key,
                            redisTtlProperties.globalDuration()
                    );

                    if (Boolean.TRUE.equals(success)) {

                        fixedCount++;

                        log.warn(
                                "Redis key '{}' had no TTL. Applied TTL of {} seconds.",
                                key,
                                redisTtlProperties.globalDuration()
                        );
                    }
                }
            }

            if (fixedCount > 0) {
                log.warn(
                        "Applied default TTL to {} Redis keys.",
                        fixedCount
                );
            } else {
                log.debug(
                        "No Redis keys without TTL detected."
                );
            }

        } catch (Exception ex) {

            log.warn(
                    "DevRedisDiagnostics failed to scan Redis keys",
                    ex
            );
        }
    }
}