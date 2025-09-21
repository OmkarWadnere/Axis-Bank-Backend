package com.axis.bank.models.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginRequest {

    @NotBlank(message = "Please enter mobileNumber or emailId")
    private String mobileNumberOrEmailId;

    @NotBlank(message = "Please enter password")
    private String password;
}
