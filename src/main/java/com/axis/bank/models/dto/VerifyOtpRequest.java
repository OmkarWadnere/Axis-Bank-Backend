package com.axis.bank.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpRequest {

    @Email(message = "please enter valid emailId")
    @NotNull(message = "emailId should not be blank")
    @Pattern(regexp = "^[^@]+@gmail\\.com$", message = "please enter valid emailId")
    private String emailId;
    @NotNull(message = "mobileNumber should not be blank")
    @Pattern(regexp = "^\\d{6}$", message = "otp should be 10 digits")
    private String otp;
}
