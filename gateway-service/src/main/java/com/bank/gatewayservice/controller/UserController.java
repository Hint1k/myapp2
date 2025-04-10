package com.bank.gatewayservice.controller;

import com.bank.gatewayservice.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "User Authentication", description = "Endpoints for JWT token issuing and user authentication")
public class UserController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates user credentials and returns a JWT token.")
    public ResponseEntity<Map<String, String>> authenticateUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        try {
            // Validate the provided credentials against the database
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Check if a valid token exists in Redis
            String cachedToken = (String) redisTemplate.opsForValue().get("token:" + username);
            if (cachedToken != null) {
                long remainingValidity = jwtService.getRemainingValidity(cachedToken);
                if (remainingValidity > 0) {
                    log.info("Token found in cache for user {}: {}", username, cachedToken);
                    log.info("Remaining validity: {} ms", remainingValidity);
                    return ResponseEntity.ok(Map.of("token", cachedToken));
                }
            }

            // Generate a new token if credentials are valid
            List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String jwtToken = jwtService.generateToken(username, roles);
            log.info("Authentication successful for user: {}", username);
            log.info("Generated new token for user {}: {}", username, jwtToken);

            // Cache the new token
            log.info("Storing token in Redis for user {}: {}", username, jwtToken);
            redisTemplate.opsForValue().set("token:" + username, jwtToken, Duration.ofMinutes(60));

            return ResponseEntity.ok(Map.of("token", jwtToken));
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            log.error("An unexpected error occurred during authentication for user {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server Error"));
        }
    }
}