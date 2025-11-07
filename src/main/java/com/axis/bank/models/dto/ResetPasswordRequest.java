package com.axis.bank.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
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
public class ResetPasswordRequest {

    @NotEmpty(message = "emailId should not be empty")
    @Email(message = "please enter valid emailId")
    @Pattern(regexp = "^[^@]+@gmail\\.com$", message = "please enter valid emailId")
    private String emailId;

    @Pattern(
            regexp = "^(?=.{8,})(?=.*\\d)(?=.*[^A-Za-z0-9])[A-Z].*$",
            message = "Password must start with an uppercase letter, be at least 8 characters long, and include at least one number and one special character."
    )
    private String newPassword;
}
