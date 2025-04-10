package com.bank.gatewayservice.service;

import java.util.List;

public interface JwtService {

    String generateToken(String username, List<String> roles);

    boolean validateToken(String token, String username);

    String extractUsername(String token);

    boolean isTokenExpired(String token);

    List<String> extractRoles(String token);

    String extractTokenFromHeader(String authHeader);

    long getRemainingValidity(String token);
}