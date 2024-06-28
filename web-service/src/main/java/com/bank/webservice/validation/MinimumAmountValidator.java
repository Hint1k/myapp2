package com.bank.webservice.validation;

import com.bank.webservice.dto.Transaction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class MinimumAmountValidator implements ConstraintValidator<MinimumAmount, Transaction> {

    private static final BigDecimal MIN_ALLOWED_AMOUNT = new BigDecimal("0.01");

    @Override
    public boolean isValid(Transaction transaction, ConstraintValidatorContext constraintValidatorContext) {
        return transaction.getAmount().compareTo(MIN_ALLOWED_AMOUNT) >= 0;
    }
}