package com.axis.bank.models.dto;

import com.axis.bank.models.emum.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SignUpRequest {

    @NotBlank(message = "firstName should not be blank")
    private String firstName;

    @NotBlank(message = "lastName should not be blank")
    private String lastName;

    @Email(message = "emailId should be valid")
    @NotNull(message = "emailId should not be blank")
    private String emailId;

    @NotNull(message = "mobileNumber should not be blank")
    @Pattern(regexp = "^\\d{10}$", message = "mobileNumber should be 10 digits")
    private String mobileNumber;

    @NotBlank(message = "password should not be blank")
    private String password;

    @PastOrPresent(message = "birthDate should not be future date")
    @NotNull(message = "birthDate should not be blank")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String role = Role.CUSTOMER.getRoleType();
}
