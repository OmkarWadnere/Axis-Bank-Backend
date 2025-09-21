package com.axis.bank.controller;

import com.axis.bank.exception.AxisBankException;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
public class AxisBankController {

    private HealthEndpoint healthEndpoint;

    private InfoEndpoint infoEndpoint;

    private MetricsEndpoint metricsEndpoint;

    private EnvironmentEndpoint environmentEndpoint;

    private ThreadDumpEndpoint threadDumpEndpoint;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getFullHealth() throws AxisBankException {
        Map<String, Object> result = new HashMap<>();

        // Health
        result.put("health", healthEndpoint.health());

        // Info
        result.put("info", infoEndpoint.info());

        // Metrics (you can add more keys if needed)
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("jvm.memory.used", metricsEndpoint.metric("jvm.memory.used", null));
        metrics.put("system.cpu.usage", metricsEndpoint.metric("system.cpu.usage", null));
        metrics.put("http.server.requests", metricsEndpoint.metric("http.server.requests", null));
        result.put("metrics", metrics);

        // Environment (be careful in prod, this may expose sensitive info)
        result.put("environment", environmentEndpoint.environment(null));

        // Thread dump
        result.put("threads", threadDumpEndpoint.threadDump());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
