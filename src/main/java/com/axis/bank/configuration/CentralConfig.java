package com.axis.bank.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class CentralConfig {

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
