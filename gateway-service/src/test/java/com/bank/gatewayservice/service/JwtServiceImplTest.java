package com.bank.gatewayservice.service;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JwtServiceImplTest {

    // Helper method to create a JwtServiceImpl instance with a specific expiration value.
    private JwtServiceImpl createJwtServiceWithExpiration(long expirationInMs) {
        try {
            // Use a secret that meets HS512 requirements
            String secret = "jwtSecretSuperSecureKeyThatIsAtLeast64CharactersLongForHS512Algorithm";
            JwtServiceImpl jwtService = new JwtServiceImpl(secret);
            Field expField = JwtServiceImpl.class.getDeclaredField("jwtExpirationInMs");
            expField.setAccessible(true);
            expField.set(jwtService, expirationInMs);
            return jwtService;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("createJwtServiceWithExpiration() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGenerateTokenAndExtractClaims() {
        // Given: A JwtServiceImpl instance with a 1-hour expiration
        JwtServiceImpl jwtService = createJwtServiceWithExpiration(5000L);
        String username = "user1";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        // When: Generating a token
        String token = jwtService.generateToken(username, roles);

        // Then: The token should not be null and claims can be extracted
        assertNotNull(token, "Token should not be null");

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername, "Extracted username should match");

        List<String> extractedRoles = jwtService.extractRoles(token);
        // Verify both lists contain the same roles (order is not guaranteed)
        assertTrue(extractedRoles.containsAll(roles) && roles.containsAll(extractedRoles),
                "Extracted roles should match the input roles");

        // The token should be valid for the correct username
        assertTrue(jwtService.validateToken(token, username), "Token should be valid");

        long remainingValidity = jwtService.getRemainingValidity(token);
        assertTrue(remainingValidity > 0, "Remaining validity should be positive");
    }

    @Test
    void testIsTokenExpired() {
        // Given: A JwtServiceImpl with a negative expiration value to force expired tokens
        JwtServiceImpl jwtService = createJwtServiceWithExpiration(-5000L);
        String token = jwtService.generateToken("user1", List.of("ROLE_USER"));

        // Then: The token should be expired
        assertTrue(jwtService.isTokenExpired(token), "Token should be expired");

        // And when validating an expired token, an ExpiredJwtException should be thrown
        assertThrows(ExpiredJwtException.class, () -> jwtService.validateToken(token, "user1"),
                "Expired token should throw an ExpiredJwtException");
    }

    @Test
    void testExtractTokenFromHeader() {
        // Given: A JwtServiceImpl instance (expiration not used in this test)
        JwtServiceImpl jwtService = createJwtServiceWithExpiration(5000L);
        String token = "sampleToken";

        // When: The header is in "Bearer <token>" format
        String header = "Bearer " + token;
        String extracted = jwtService.extractTokenFromHeader(header);
        // Then: The extracted token should match
        assertEquals(token, extracted, "Extracted token should match the original token");

        // And when: The token is wrapped in a JSON object format
        String jsonHeader = "Bearer {\"token\":\"" + token + "\"}";
        String extractedJson = jwtService.extractTokenFromHeader(jsonHeader);
        assertEquals(token, extractedJson, "Extracted token from JSON header should match");
    }

    @Test
    void testValidateToken_WrongUsername() {
        // Given: A valid token generated for "user1"
        JwtServiceImpl jwtService = createJwtServiceWithExpiration(5000L);
        String token = jwtService.generateToken("user1", List.of("ROLE_USER"));

        // When & Then: Validation should fail for a different username
        assertFalse(jwtService.validateToken(token, "user2"),
                "Token should be invalid for a different username");
    }

    @Test
    void testExtractRoles_NoRolesClaim() {
        // Given: A token generated without roles
        JwtServiceImpl jwtService = createJwtServiceWithExpiration(5000L);
        String token = jwtService.generateToken("user1", List.of());

        // When: Extracting roles
        List<String> extractedRoles = jwtService.extractRoles(token);

        // Then: Should return an empty list
        assertNotNull(extractedRoles, "Extracted roles should not be null");
        assertTrue(extractedRoles.isEmpty(), "Extracted roles should be an empty list");
    }
}