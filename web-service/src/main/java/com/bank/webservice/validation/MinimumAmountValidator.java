package com.bank.webservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class MinimumAmountValidator implements ConstraintValidator<MinimumAmount, BigDecimal> {

    private static final BigDecimal MIN_ALLOWED_AMOUNT = new BigDecimal("0.01");

    @Override
    public boolean isValid(BigDecimal amount, ConstraintValidatorContext constraintValidatorContext) {
        if (amount == null) {
            return true; // to avoid interference with the @NotNull annotation
        }
        return amount.compareTo(MIN_ALLOWED_AMOUNT) >= 0;
    }
}