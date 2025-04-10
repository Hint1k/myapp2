package com.bank.gatewayservice.service.impl;

import com.bank.gatewayservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public JwtServiceImpl(@Value("${jwt.secret}") String secretKey) {
        // Generate a SecretKey instance from the provided string
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @Override
    public String generateToken(String username, List<String> roles) {
        return Jwts.builder().subject(username).claim("roles", roles).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(secretKey, Jwts.SIG.HS512).compact(); // HS512 is an algorithm for signing tokens
    }

    @Override
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // Token is expired
        }
    }

    @Override
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<?> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public String extractTokenFromHeader(String authHeader) {
        String token = authHeader.substring(7);
        if (token.startsWith("{\"token\":\"") && token.endsWith("\"}")) {
            return token.substring(10, token.length() - 2);
        }
        return token;
    }

    @Override
    public long getRemainingValidity(String token) {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        return expirationDate.getTime() - System.currentTimeMillis();
    }
}