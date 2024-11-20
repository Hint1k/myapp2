package com.bank.gatewayservice.controller;

import com.bank.gatewayservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AuthController(JwtUtil jwtUtil, RedisTemplate<String, Object> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid Authorization header"));
            }

            // Extract the token from the header
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            log.info("Received token in verify endpoint: {}", token);

            // Check Redis for cached validation result
            Object cachedObject = redisTemplate.opsForValue().get("token:" + token);
            if (cachedObject != null) {
                try {
                    if (cachedObject instanceof Map<?, ?> cachedMap) {
                        @SuppressWarnings("unchecked") // Safe since we checked the type
                        Map<String, Object> cachedResponse = (Map<String, Object>) cachedMap;
                        log.info("Token validation result found in cache for token: {}", token);
                        return ResponseEntity.ok(cachedResponse);
                    } else {
                        log.warn("Cached object for token is not of expected type: {}", token);
                    }
                } catch (ClassCastException e) {
                    log.error("Error casting cached object to Map for token: {}", token, e);
                }
            } else {
                log.info("No cached result found for token: {}", token);
            }

            // If token is not in cache, retrieve all keys (tokens) from Redis
            log.info("No cached result found for token, fetching all tokens from Redis...");
            Set<String> allKeys = redisTemplate.keys("*");  // Get all keys in Redis (for testing purposes)
            log.debug("All Redis keys (tokens): {}", allKeys);  // Log the found keys

            // Validate the token if not cached
            String username = jwtUtil.extractUsername(token);
            if (!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            // Extract roles and prepare response
            List<String> roles = jwtUtil.extractRoles(token);
            Map<String, Object> response = Map.of("username", username, "roles", roles);

            // Get the remaining token validity duration in milliseconds
            long remainingValidity = jwtUtil.getRemainingValidity(token);
            Duration cacheDuration = Duration.ofMillis(remainingValidity);

            // Cache the validation result in Redis for the remaining token validity
            redisTemplate.opsForValue().set("token:" + token, response, cacheDuration);
            log.info("Token validation result cached for token: {}", token);
            log.info("Cached token for duration: {} milliseconds", cacheDuration.toMillis());
            log.info("Remaining validity: {} ms", remainingValidity);
            log.info("Serialized token: {}", redisTemplate.opsForValue().get("token:" + token));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token verification failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token verification failed"));
        }
    }
}