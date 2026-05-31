package com.axis.bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.redis.ttl")
public class RedisTtlProperties {

    private Long signupVerificationMinutes = 15L;
    private Long otpSeconds = 300L;
    private Long cooldownSeconds = 60L;
    private Long requestCountMinutes = 5L;
    private Long globalSeconds = 5L;

    public Long getSignupVerificationMinutes() {
        return signupVerificationMinutes;
    }

    public void setSignupVerificationMinutes(Long signupVerificationMinutes) {
        this.signupVerificationMinutes = signupVerificationMinutes;
    }

    public Long getOtpSeconds() {
        return otpSeconds;
    }

    public void setOtpSeconds(Long otpSeconds) {
        this.otpSeconds = otpSeconds;
    }

    public Long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(Long cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public Long getRequestCountMinutes() {
        return requestCountMinutes;
    }

    public Long getGlobalSeconds() {
        return globalSeconds;
    }

    public void setGlobalSeconds(Long globalSeconds) {
        this.globalSeconds = globalSeconds;
    }

    public Duration globalDuration() {
        return Duration.ofSeconds(globalSeconds);
    }

    public void setRequestCountMinutes(Long requestCountMinutes) {
        this.requestCountMinutes = requestCountMinutes;
    }

    public Duration signupVerificationDuration() {
        return Duration.ofMinutes(signupVerificationMinutes);
    }

    public Duration otpDuration() {
        return Duration.ofSeconds(otpSeconds);
    }

    public Duration cooldownDuration() {
        return Duration.ofSeconds(cooldownSeconds);
    }

    public Duration requestCountDuration() {
        return Duration.ofMinutes(requestCountMinutes);
    }
}

