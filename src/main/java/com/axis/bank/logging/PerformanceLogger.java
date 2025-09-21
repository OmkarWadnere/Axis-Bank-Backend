package com.axis.bank.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceLogger.class);

    // Match every public method in com.axis.bank and all its subpackages
    @Around("execution(* com.axis.bank..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            LOGGER.info("Method={} executed in {} ms",
                    joinPoint.getSignature().toShortString(), duration);
        }
    }
}
