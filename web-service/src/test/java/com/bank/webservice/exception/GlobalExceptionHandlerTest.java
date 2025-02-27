package com.bank.webservice.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import jakarta.servlet.ServletException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private Model model;

    @Test
    void testHandleUnauthorizedException() {
        // Arrange
        UnauthorizedException ex = new UnauthorizedException("Access Denied");

        // Act
        String viewName = globalExceptionHandler.handleUnauthorizedException(ex, model);

        // Assert
        assertEquals("access-denied", viewName, "View name should be 'access-denied'");
        verify(model, times(1))
                .addAttribute(eq("errorMessage"), eq("Access Denied"));
    }

    @Test
    void testHandleServletException() {
        // Arrange
        ServletException ex = new ServletException("Servlet error");

        // Act
        String viewName = globalExceptionHandler.handleServletException(ex, model);

        // Assert
        assertEquals("error", viewName, "View name should be 'error'");
        verify(model, times(1))
                .addAttribute(eq("errorMessage"), eq("Server error. Please try again later."));
    }

    @Test
    void testHandleIOException() {
        // Arrange
        IOException ex = new IOException("IO error");

        // Act
        String viewName = globalExceptionHandler.handleIOException(ex, model);

        // Assert
        assertEquals("error", viewName, "View name should be 'error'");
        verify(model, times(1))
                .addAttribute(eq("errorMessage"), eq("Server error. Please try again later."));
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        Exception ex = new RuntimeException("Something went wrong");

        // Act
        String viewName = globalExceptionHandler.handleGenericException(ex, model);

        // Assert
        assertEquals("error", viewName, "View name should be 'error'");
        verify(model, times(1))
                .addAttribute(eq("errorMessage"), eq("Something went wrong"));
    }
}