package com.bank.webservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class ProxyServiceImpl implements ProxyService {

    private final RestTemplate restTemplate; // Synchronous RestTemplate
    private static final String VERIFY_URL = "http://gateway-service:8080/verify";

    @Autowired
    public ProxyServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void addUserInfoToModel(String token, Model model) {
        log.info("ProxyService: Validating token");
        log.info("Preparing to send request to VERIFY_URL with Authorization header: Bearer {}", token);

        try {
            // Set the Authorization header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Synchronous call to verify token validity
            restTemplate.exchange(VERIFY_URL, HttpMethod.GET, entity, Map.class);

            // Simulate extracting user info if token is valid
            model.addAttribute("username", "admin");
            model.addAttribute("roles", "ADMIN");
        } catch (Exception e) {
            log.error("Error communicating with gateway-service for token validation", e);
            model.addAttribute("errorMessage", "Token validation failed.");
        }
    }
}