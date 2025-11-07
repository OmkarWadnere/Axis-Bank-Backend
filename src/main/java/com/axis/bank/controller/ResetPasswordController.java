package com.axis.bank.controller;

import com.axis.bank.exception.AxisBankException;
import com.axis.bank.models.dto.OtpRequest;
import com.axis.bank.models.dto.OtpResponse;
import com.axis.bank.models.dto.ResetPasswordRequest;
import com.axis.bank.models.dto.ResetPasswordResponse;
import com.axis.bank.models.dto.VerifyOtpRequest;
import com.axis.bank.models.dto.VerifyOtpResponse;
import com.axis.bank.service.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/reset-password")
@AllArgsConstructor
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    @PostMapping("/generate-otp")
    public ResponseEntity<OtpResponse> requestOtp(@Valid @RequestBody OtpRequest otpRequest) throws AxisBankException, NoSuchAlgorithmException {
        return new ResponseEntity<>(resetPasswordService.generateOtpAndSendOtp(otpRequest), HttpStatus.CREATED);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) throws AxisBankException {
        return new ResponseEntity<>(resetPasswordService.verifyOtp(verifyOtpRequest), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) throws AxisBankException {
        return new ResponseEntity<>(resetPasswordService.resetPassword(resetPasswordRequest), HttpStatus.OK);
    }

}
