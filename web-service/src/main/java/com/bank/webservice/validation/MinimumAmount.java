package com.bank.webservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MinimumAmountValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinimumAmount {

    String value() default "";

    String message() default "The minimum amount must be equal or greater than 0.01";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}