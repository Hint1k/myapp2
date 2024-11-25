package com.bank.gatewayservice.controller;

import com.bank.gatewayservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/proxy")
@Slf4j
public class ProxyController {

    private final WebClient webClient;
    private final JwtUtil jwtUtil;
    private static final String WEB_SERVICE_URL = "http://web-service:8080";

    @Autowired
    public ProxyController(WebClient webClient, JwtUtil jwtUtil) {
        this.webClient = webClient;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/**")
    public Mono<ResponseEntity<String>> proxyToWebServiceGet(HttpServletRequest request) {
        return proxyToWebService(request, null);
    }

    @PostMapping("/**")
    public Mono<ResponseEntity<String>> proxyToWebServicePost(
            @RequestBody Map<String, Object> body, HttpServletRequest request) {
        return proxyToWebService(request, body);
    }

    private Mono<ResponseEntity<String>> proxyToWebService(HttpServletRequest request, Map<String, Object> body) {
        log.info("proxyToWebService is invoked");
        String targetUrl = WEB_SERVICE_URL + request.getRequestURI().replace("/proxy", "");
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Validate the token using JwtUtil
        ResponseEntity<String> tokenValidationResponse = validateToken(authHeader);
        if (tokenValidationResponse != null) {
            return Mono.just(tokenValidationResponse);
        }

        // Determine whether to perform a GET or POST request and set the body accordingly
        if (body == null) {
            // For GET requests
            return webClient.get()
                    .uri(targetUrl)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authHeader))
                    .retrieve()
                    .toEntity(String.class);
        } else {
            // For POST requests
            return webClient.post()
                    .uri(targetUrl)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authHeader))
                    .body(BodyInserters.fromValue(body))  // Insert the body using BodyInserters
                    .retrieve()
                    .toEntity(String.class);
        }
    }

    private ResponseEntity<String> validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing token");
        }

        String token = jwtUtil.extractTokenFromHeader(authHeader);
        log.info("Extracted token: {}", token);
        if (!isTokenValid(token)) {
            log.warn("Token validation failed for token: {}", token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or expired token");
        }
        return null;  // Valid token
    }

    private boolean isTokenValid(String token) {
        log.info("Token validation runs for token: {}", token);
        try {
            String username = jwtUtil.extractUsername(token);
            log.info("Extracted username: {}", username);
            boolean valid = jwtUtil.validateToken(token, username);
            if (valid) {
                log.info("Token is valid for user: {}", username);
            } else {
                log.warn("Token is invalid for user: {}", username);
            }
            return valid;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
}