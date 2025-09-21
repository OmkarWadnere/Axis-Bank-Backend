package com.axis.bank.models.emum;

import com.axis.bank.exception.AxisBankException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Role {

    ADMIN("ADMIN"),
    EMPLOYEE("EMPLOYEE"),
    CUSTOMER("CUSTOMER");

    private String roleType;

    public Role getRole(String role) throws AxisBankException {
        return Arrays.stream(Role.values()).filter(r -> r.getRoleType().equals(role)).findFirst()
                .orElseThrow(() -> new AxisBankException("Invalid role", HttpStatus.BAD_REQUEST));
    }
}
