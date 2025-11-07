package com.axis.bank.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import static com.axis.bank.utility.Constants.BLOOM_NAME;

@Service
@RequiredArgsConstructor
public class BloomFilterService {

    private final RedissonClient redissonClient;

    @PostConstruct
    public void setup() {
        init(100000000L, 0.01);
    }

    public void init(long expectedInsertion, double falsePositiveProbability) {
        RBloomFilter<String> bloom = redissonClient.getBloomFilter(BLOOM_NAME);
        bloom.tryInit(expectedInsertion, falsePositiveProbability);
    }

    public boolean mightExist(String key) {
        RBloomFilter<String> bloom = redissonClient.getBloomFilter(BLOOM_NAME);
        return bloom.contains(key);
    }

    public void add(String... keys) {
        RBloomFilter<String> bloom = redissonClient.getBloomFilter(BLOOM_NAME);
        for (String key : keys) {
            bloom.add(key);
        }
    }
}
