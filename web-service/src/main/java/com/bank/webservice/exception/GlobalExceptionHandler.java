package com.bank.webservice.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handling Unauthorized Exception
    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedException(UnauthorizedException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "access-denied";
    }

    // Handling generic exceptions
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {

        String errorMessage = ex.getMessage();
        model.addAttribute("errorMessage", errorMessage);
        return "error";
    }
}