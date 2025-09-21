package com.axis.bank.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class AxisBankException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;
    private HttpStatus httpStatus;

    public AxisBankException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getStatus() {
        return this.httpStatus;
    }
}