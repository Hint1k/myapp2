package com.bank.webservice.exception;

import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handling Unauthorized Exception
    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedException(UnauthorizedException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "access-denied";
    }

    // Handling ServletException
    @ExceptionHandler(ServletException.class)
    public String handleServletException(ServletException ex, Model model) {
        log.error("ServletException occurred: {}", ex.getMessage(), ex); // Log full stack trace
        model.addAttribute("errorMessage", "Server error. Please try again later.");
        return "error"; // Redirect to generic error page
    }

    // Handling IOException
    @ExceptionHandler(IOException.class)
    public String handleIOException(IOException ex, Model model) {
        log.error("IOException occurred: {}", ex.getMessage(), ex); // Log full stack trace
        model.addAttribute("errorMessage", "Server error. Please try again later.");
        return "error"; // Redirect to generic error page
    }

    // Handling generic exceptions
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {

        String errorMessage = ex.getMessage();
        model.addAttribute("errorMessage", errorMessage);
        return "error";
    }
}