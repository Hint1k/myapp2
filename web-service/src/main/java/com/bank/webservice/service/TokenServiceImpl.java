package com.bank.webservice.service;

import com.bank.webservice.dto.UserResponse;
import com.bank.webservice.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final RestTemplate restTemplate; // Synchronous RestTemplate
    private static final String VERIFY_URL = "http://gateway-service:8080/verify";

    @Autowired
    public TokenServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public UserResponse validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            // Fetch the current request URI using RequestContextHolder
            String originalUri = null;
            if (RequestContextHolder.getRequestAttributes() != null) {
                originalUri = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest().getRequestURI();
            }

            // Conditionally add X-Original-URI header only for non-login requests
            if (originalUri != null && !originalUri.equals("/login")) {
                headers.set("X-Original-URI", originalUri);  // Add X-Original-URI header
            }

            // Log the headers before making the request
            log.info("Headers sent to verify endpoint:");
            String authHeader = headers.getFirst("Authorization");
            if (authHeader != null) {
                log.info("Header: Authorization -> {}", authHeader);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<UserResponse> responseEntity = restTemplate
                    .exchange(VERIFY_URL, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
                    });

            // Log the response body
            if (responseEntity.getBody() == null) {
                log.error("Received null response body from gateway-service for token validation.");
            } else {
                log.info("Received response from gateway-service: {}", responseEntity.getBody());
            }

            // Extract and log the customer number
            HttpHeaders responseHeaders = responseEntity.getHeaders();
            String customerNumber = responseHeaders.getFirst("X-Customer-Number");
            log.info("Customer number in TokenServiceImpl: {}", customerNumber);

            // Store in the current request context so it is accessible in FilterServiceImpl
            if (customerNumber != null && RequestContextHolder.getRequestAttributes() != null) {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                attributes.getRequest().setAttribute("X-Customer-Number", customerNumber);
            }

            return responseEntity.getBody();
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Access denied: {}", e.getMessage());
            throw new UnauthorizedException("Access denied: You do not have permission to access this resource.");
        } catch (Exception e) {
            log.error("Error during token validation", e);
            throw e;
        }
    }
}