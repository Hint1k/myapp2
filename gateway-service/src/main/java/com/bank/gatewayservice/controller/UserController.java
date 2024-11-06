package com.bank.gatewayservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
public class UserController {

    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login") //TODO do I need "login" or "authenticate" or another endpoint?
    public ResponseEntity<String> authenticateUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        log.info("Attempting authentication for user: {}, {}", username, password);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            log.info("Authentication successful for user: {}", username);
            return ResponseEntity.ok("Login successful");
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}