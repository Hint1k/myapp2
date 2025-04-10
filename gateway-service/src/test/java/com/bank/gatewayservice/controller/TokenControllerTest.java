package com.bank.gatewayservice.controller;

import com.bank.gatewayservice.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenControllerTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TokenController tokenController;

    private String validToken;
    private String invalidToken;
    private String validAuthHeader;
    private String invalidAuthHeader;

    @BeforeEach
    void setUp() {
        validToken = "valid_token";
        invalidToken = "invalid_token";
        validAuthHeader = "Bearer " + validToken;
        invalidAuthHeader = "Bearer " + invalidToken;

        // Initialize the TokenController and inject mocked JwtService
        tokenController = new TokenController(jwtService);
    }

    @Test
    public void testVerifyToken_ValidToken() {
        // Given: A valid token and mock JwtService behavior
        String username = "user123";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        when(jwtService.extractTokenFromHeader(validAuthHeader)).thenReturn(validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(username);
        when(jwtService.validateToken(validToken, username)).thenReturn(true);
        when(jwtService.extractRoles(validToken)).thenReturn(roles);

        // When: Calling the verifyToken method
        ResponseEntity<Map<String, Object>> response = tokenController.verifyToken(validAuthHeader);

        // Then: Verify the response is successful and contains the expected data
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("username"));
        assertEquals("user123", response.getBody().get("username"));
        assertTrue(response.getBody().containsKey("roles"));
        assertEquals(roles, response.getBody().get("roles"));
    }

    @Test
    public void testVerifyToken_InvalidToken() {
        // Given: An invalid token
        when(jwtService.extractTokenFromHeader(invalidAuthHeader)).thenReturn(invalidToken);
        when(jwtService.extractUsername(invalidToken)).thenReturn("user123");
        when(jwtService.validateToken(invalidToken, "user123")).thenReturn(false);

        // When: Calling the verifyToken method
        ResponseEntity<Map<String, Object>> response = tokenController.verifyToken(invalidAuthHeader);

        // Then: Verify the response is unauthorized with the correct error message
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("Invalid or expired token", response.getBody().get("error"));
    }

    @Test
    public void testVerifyToken_MissingAuthorizationHeader() {
        // Given: Missing Authorization header
        String missingAuthHeader = null;

        // When: Calling the verifyToken method
        ResponseEntity<Map<String, Object>> response = tokenController.verifyToken(null);

        // Then: Verify the response is unauthorized with the correct error message
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("Missing or invalid Authorization header", response.getBody().get("error"));
    }

    @Test
    public void testVerifyToken_ExpiredToken() {
        // Given: An expired token scenario
        when(jwtService.extractTokenFromHeader(validAuthHeader)).thenReturn(validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("user123");
        when(jwtService.validateToken(validToken, "user123")).thenReturn(false);

        // When: Calling the verifyToken method
        ResponseEntity<Map<String, Object>> response = tokenController.verifyToken(validAuthHeader);

        // Then: Verify the response is unauthorized with the correct error message
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("Invalid or expired token", response.getBody().get("error"));
    }

    @Test
    public void testVerifyToken_ExceptionHandling() {
        // Given: An exception during token verification
        when(jwtService.extractTokenFromHeader(validAuthHeader)).thenThrow(new RuntimeException("Unexpected error"));

        // When: Calling the verifyToken method
        ResponseEntity<Map<String, Object>> response = tokenController.verifyToken(validAuthHeader);

        // Then: Verify the response is internal server error with the correct error message
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("Token verification failed", response.getBody().get("error"));
    }
}