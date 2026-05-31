package com.axis.bank.models.emum;

import com.axis.bank.exception.AxisBankException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MathematicalOperation {

    ADDITION("Addition"),
    SUBTRACTION("Subtraction"),
    MULTIPLICATION("Multiplication"),
    NEUTRALIZE("Neutralize");

    private String value;

    public static MathematicalOperation fromValue(String value) throws AxisBankException {
        for (MathematicalOperation mathematicalOperation : values()) {
            if (Strings.CS.equals(value, mathematicalOperation.getValue())) {
                return mathematicalOperation;
            }
        }
        throw new AxisBankException("Unsupported enum value: " + value, HttpStatus.BAD_REQUEST);
    }
}
