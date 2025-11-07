package com.axis.bank.models.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MathematicalOperation {

    ADDITION("Addition"),
    SUBTRACTION("Subtraction"),
    NEUTRALIZE("Neutralize");

    private String value;
}
