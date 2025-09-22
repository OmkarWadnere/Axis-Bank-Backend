package com.axis.bank.auth.security;

import com.axis.bank.exception.model.ErrorInfo;
import com.axis.bank.utility.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.axis.bank.utility.Constants.OPERATION_ID;
import static com.axis.bank.utility.Constants.X_OPERATION_ID;
import static com.axis.bank.utility.Constants.X_TRACE_ID;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        MDC.put(Constants.TRACE_ID, request.getHeader(X_TRACE_ID) == null ? UUID.randomUUID().toString() : request.getHeader(X_TRACE_ID));
        MDC.put(OPERATION_ID, request.getHeader(X_OPERATION_ID) == null ? UUID.randomUUID().toString() : request.getHeader(X_OPERATION_ID));
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setUuid(MDC.get(Constants.TRACE_ID));
        errorInfo.setErrorMessage("Access denied: " + accessDeniedException.getMessage());
        errorInfo.setErrorCode(HttpStatus.FORBIDDEN.value());
        errorInfo.setTimeStamp(LocalDateTime.now());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorInfo));
    }
}
