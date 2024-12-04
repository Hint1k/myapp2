package com.bank.webservice.service;

import com.bank.webservice.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<UserResponse> responseEntity = restTemplate
                    .exchange(VERIFY_URL, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Error during token validation", e);
            throw e;
        }
    }
}