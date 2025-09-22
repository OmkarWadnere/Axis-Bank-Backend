package com.axis.bank.controller;

import com.axis.bank.exception.AxisBankException;
import com.axis.bank.models.dto.LoginRequest;
import com.axis.bank.models.dto.LoginResponse;
import com.axis.bank.models.dto.SignUpRequest;
import com.axis.bank.models.dto.SignUpResponse;
import com.axis.bank.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Validated
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> singUpUser(@Valid @RequestBody SignUpRequest signUpRequest) throws AxisBankException {
        return new ResponseEntity<>(userService.signUpUser(signUpRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) throws AxisBankException {
        return new ResponseEntity<>(userService.    loginUser(loginRequest), HttpStatus.OK);
    }
}
