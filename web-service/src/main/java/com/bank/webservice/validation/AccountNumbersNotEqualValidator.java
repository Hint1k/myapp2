package com.bank.webservice.validation;

import com.bank.webservice.dto.Transaction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class AccountNumbersNotEqualValidator implements ConstraintValidator<AccountNumbersNotEqual, Transaction> {

    @Override
    public boolean isValid(Transaction transaction, ConstraintValidatorContext constraintValidatorContext) {
        return !Objects.equals(transaction.getAccountDestinationNumber(), transaction.getAccountSourceNumber());
    }
}