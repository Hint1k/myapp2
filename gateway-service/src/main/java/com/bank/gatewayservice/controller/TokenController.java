package com.bank.gatewayservice.controller;

import com.bank.gatewayservice.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "User Authorization", description = "Endpoints for JWT token verification and user authorization")
public class TokenController {

    private final JwtService jwtService;

    @GetMapping("/verify")
    @Operation(summary = "Verify JWT token",
            description = "Validates the provided JWT token and returns the associated user roles.",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer token for authentication",
                            in = ParameterIn.HEADER, required = true, example = "Bearer your_jwt_token_here")
            }
    )
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Verification request received: {}", authHeader);
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid Authorization header"));
            }

            // Extract the token from the header
            String token = jwtService.extractTokenFromHeader(authHeader);
            log.info("Received token in verify endpoint: {}", token);

            // Direct token validation via JwtUtil
            String username = jwtService.extractUsername(token);
            if (!jwtService.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            // Extract roles and prepare response
            List<String> roles = jwtService.extractRoles(token);
            Map<String, Object> response = Map.of("username", username, "roles", roles);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token verification failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token verification failed"));
        }
    }
}