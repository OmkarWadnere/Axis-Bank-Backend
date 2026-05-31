package com.axis.bank.config;

import com.axis.bank.utility.Constants;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.axis.bank.utility.Constants.X_TRACE_ID;

@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Wrap request/response to read body multiple times
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String traceId = request.getHeader(X_TRACE_ID);
        MDC.put(Constants.TRACE_ID, traceId);

        try {
            // Continue filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Log request and response
            logRequest(wrappedRequest);
            logResponse(wrappedResponse);

            // Copy response body back to output stream
            wrappedResponse.copyBodyToResponse();

            // Clear MDC
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String body = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
        String maskedBody = maskSensitiveFields(body);

        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            String value = header.equalsIgnoreCase("Authorization") ? "****" : request.getHeader(header);
            headers.put(header, value);
        }

        log.debug("Request: {} {}\nHeaders: {}\nBody: {}",
                request.getMethod(),
                request.getRequestURI(),
                CollectionUtils.isNotEmpty(headers.entrySet()) ?
                        headers.entrySet().stream()
                                .map(entry -> entry.getKey() + ": " + entry.getValue())
                                .collect(Collectors.joining(", "))
                        : null,
                maskedBody);
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        String body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        String maskedBody = maskSensitiveFields(body);

        Map<String, String> headers = new HashMap<>();
        for (String header : response.getHeaderNames()) {
            String value = header.equalsIgnoreCase("Set-Cookie") ? "****" : response.getHeader(header);
            headers.put(header, value);
        }

        log.debug("Response: Status={} Headers={} Body={}",
                response.getStatus(),
                CollectionUtils.isNotEmpty(headers.entrySet()) ?
                        headers.entrySet().stream()
                                .map(entry -> entry.getKey() + ": " + entry.getValue())
                                .collect(Collectors.joining(", "))
                        : null,
                maskedBody);
    }

    private String maskSensitiveFields(String body) {
        if (body == null || body.isEmpty()) return body;

        return body
                .replaceAll("(?i)\"[^\"]*password[^\"]*\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"")
                .replaceAll("(?i)\"[^\"]*token[^\"]*\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"")
                .replaceAll("(?i)\"[^\"]*secret[^\"]*\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"***\"")
                .replaceAll("(?i)\"[^\"]*cvv[^\"]*\"\\s*:\\s*\"[^\"]*\"", "\"cvv\":\"***\"")
                .replaceAll("(?i)\"[^\"]*pan[^\"]*\"\\s*:\\s*\"[^\"]*\"", "\"pan\":\"***\"");
    }
}
