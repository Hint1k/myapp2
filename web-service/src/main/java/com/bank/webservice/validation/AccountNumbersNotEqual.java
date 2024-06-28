package com.bank.webservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AccountNumbersNotEqualValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountNumbersNotEqual {

    String value() default "";

    String message() default "Source and Destination account numbers cannot be equal";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}