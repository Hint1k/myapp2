package com.bank.gatewayservice.controller;

import com.bank.gatewayservice.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private UserController userController;

    private String username;
    private String password;
    private Authentication authentication;
    private List<GrantedAuthority> authorities;
    private String cachedToken;

    @BeforeEach
    void setUp() {
        username = "user123";
        password = "password123";
        cachedToken = "cached_jwt_token";

        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        userController = new UserController(jwtService, authenticationManager, redisTemplate);
    }

    @Test
    void testAuthenticateUser_ValidCredentials() {
        // Mock Authentication to return the authorities
        authentication = mock(Authentication.class);
        doReturn(authorities).when(authentication).getAuthorities();

        // Given: Valid credentials and mocked successful authentication
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        String generatedToken = "generated_jwt_token";
        when(jwtService.generateToken(username, authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())))
                .thenReturn(generatedToken);

        // When: Calling the authenticateUser method
        ResponseEntity<Map<String, String>> response = userController
                .authenticateUser(Map.of("username", username, "password", password));

        // Then: Verify the response contains the generated token
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("token"));
        assertEquals(generatedToken, response.getBody().get("token"));
    }

    @Test
    void testAuthenticateUser_CachedTokenValid() {
        // Given: Valid credentials and token exists in Redis
        when(redisTemplate.opsForValue().get("token:" + username)).thenReturn(cachedToken);

        // Mock the JwtService to return the remaining validity of the token
        when(jwtService.getRemainingValidity(cachedToken)).thenReturn(1000L);

        // When: Calling the authenticateUser method
        ResponseEntity<Map<String, String>> response = userController
                .authenticateUser(Map.of("username", username, "password", password));

        // Then: Verify the response contains the cached token
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("token"));
        assertEquals(cachedToken, response.getBody().get("token"));
    }

    @Test
    void testAuthenticateUser_InvalidCredentials() {
        // Given: Invalid credentials
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials")); // Use BadCredentialsException

        // When: Calling the authenticateUser method
        ResponseEntity<Map<String, String>> response = userController
                .authenticateUser(Map.of("username", username, "password", password));

        // Then: Verify the response is Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("Invalid credentials", response.getBody().get("error"));
    }

    @Test
    void testAuthenticateUser_ServerError() {
        // Given: Simulate a server-side failure (e.g., unexpected issue in JWT service)
        authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Throwing a different unchecked exception to trigger the server error catch block
        when(jwtService.generateToken(anyString(), anyList()))
                .thenThrow(new IllegalStateException("Server error"));

        // When: Calling the authenticateUser method
        ResponseEntity<Map<String, String>> response = userController
                .authenticateUser(Map.of("username", username, "password", password));

        // Then: Verify the response is Internal Server Error
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("Server Error", response.getBody().get("error"));
    }

    @Test
    void testAuthenticateUser_CachedTokenExpired() {
        // Given: Valid credentials and mocked successful authentication
        // Mock Authentication to return the authorities
        authentication = mock(Authentication.class);
        doReturn(authorities).when(authentication).getAuthorities();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Given: Valid credentials and token exists in Redis but is expired
        when(redisTemplate.opsForValue().get("token:" + username)).thenReturn(cachedToken);

        // Mock the JwtService to return expired token
        when(jwtService.getRemainingValidity(cachedToken)).thenReturn(0L);

        // Mock generateToken to return a valid JWT token
        String newGeneratedToken = "new_generated_jwt_token";  // Ensure this is a valid token
        when(jwtService.generateToken(anyString(), anyList())).thenReturn(newGeneratedToken);

        // When: Calling the authenticateUser method
        ResponseEntity<Map<String, String>> response = userController
                .authenticateUser(Map.of("username", username, "password", password));

        // Then: Verify the response contains the newly generated token
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("token"));
        assertEquals(newGeneratedToken, response.getBody().get("token"));
    }
}