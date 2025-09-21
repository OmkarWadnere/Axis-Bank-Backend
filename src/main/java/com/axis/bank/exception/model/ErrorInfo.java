package com.axis.bank.exception.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ErrorInfo {

    private String uuid;
    private String errorMessage;
    private Integer errorCode;
    private LocalDateTime timeStamp;
}
