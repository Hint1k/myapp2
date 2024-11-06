package com.bank.webservice.controller;

import com.bank.webservice.dto.Credentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Controller
public class LoginController {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Autowired
    public LoginController(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password, Model model) {
        String gatewayUrl = "http://gateway-service:8080/login";
        Credentials credentials = new Credentials(username, password);
        credentials.setUsername(username);
        credentials.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        try {
            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(credentials), headers);
            ResponseEntity<String> response =
                    restTemplate.exchange(gatewayUrl, HttpMethod.POST, requestEntity, String.class);

            if (Objects.requireNonNull(response.getBody()).contains("authenticated")) {
                return "redirect:/index";
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "gateway/login";
            }
        } catch (JsonProcessingException e) {
            model.addAttribute("error", "An error occurred while processing your request.");
            return "gateway/login";
        }
    }
}