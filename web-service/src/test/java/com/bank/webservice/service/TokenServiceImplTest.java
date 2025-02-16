package com.bank.webservice.service;

import com.bank.webservice.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Test
    void testValidateToken_Success() {
        // Given: A valid token and expected response from the verification endpoint
        String token = "validToken";
        String url = "http://gateway-service:8080/verify";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UserResponse mockResponse = new UserResponse("John Doe", List.of("ROLE_USER"));
        ResponseEntity<UserResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        // Mocking: Simulate the external service returning a valid user response
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(entity),
                ArgumentMatchers.<ParameterizedTypeReference<UserResponse>>any())).thenReturn(responseEntity);

        // When: Calling validateToken()
        UserResponse result = tokenService.validateToken(token);

        // Then: Ensure the result matches expected user details
        assertNotNull(result, "UserResponse should not be null");
        assertEquals("John Doe", result.getUsername(), "Username should match expected value");

        // Verify that the request was made exactly once
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.GET), eq(entity),
                Mockito.<ParameterizedTypeReference<Map<String, String>>>any());
    }

    @Test
    void testValidateToken_Unauthorized() {
        // Given: An invalid token that causes the verification endpoint to return 403 Forbidden
        String token = "invalidToken";
        String url = "http://gateway-service:8080/verify";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Mocking: Simulate unauthorized response from the external service
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(entity),
                ArgumentMatchers.<ParameterizedTypeReference<UserResponse>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When & Then: Expect an HttpClientErrorException with 403 status
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
                () -> tokenService.validateToken(token));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode(), "Expected 403 Forbidden error");

        // Verify that the request was made exactly once
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.GET), eq(entity),
                Mockito.<ParameterizedTypeReference<Map<String, String>>>any());
    }
}