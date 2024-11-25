package com.bank.gatewayservice.controller;

import com.bank.gatewayservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public UserController(JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                          RedisTemplate<String, Object> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/login")
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
                long remainingValidity = jwtUtil.getRemainingValidity(cachedToken);
                if (remainingValidity > 0) {
                    log.info("Token found in cache for user {}: {}", username, cachedToken);
                    log.info("Remaining validity: {} ms", remainingValidity);
                    return ResponseEntity.ok(Map.of("token", cachedToken));
                }
            }

            // Generate a new token if credentials are valid
            List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String jwtToken = jwtUtil.generateToken(username, roles);
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