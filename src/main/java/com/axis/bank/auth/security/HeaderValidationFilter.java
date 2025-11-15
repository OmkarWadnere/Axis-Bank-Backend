package com.axis.bank.auth.security;

import com.axis.bank.exception.model.ErrorInfo;
import com.axis.bank.utility.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class HeaderValidationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        List<String> errorMessages = new ArrayList<>();
        if (StringUtils.isBlank(request.getHeader(Constants.X_TRACE_ID))) {
            errorMessages.add("Missing required header 'X-traceId'");
        }
        if (StringUtils.isBlank(request.getHeader(Constants.X_OPERATION_ID))) {
            errorMessages.add("Missing required header 'X-operationId'");
        }
        if (errorMessages.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setErrorCode(HttpStatus.BAD_REQUEST.value());
        errorInfo.setTimeStamp(LocalDateTime.now());
        errorInfo.setErrorMessages(errorMessages);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorInfo));
    }
}
