package com.bank.webservice.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@Slf4j
public class LoginController {

    private final RestTemplate restTemplate; // Synchronous RestTemplate
    private static final String URL = "http://gateway-service:8080/login";

    @Autowired
    public LoginController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model,
                        HttpSession session) {
        Map<String, String> credentials = Map.of("username", username, "password", password);

        try {
            // Synchronous call to gateway-service for login using ParameterizedTypeReference
            Map<String, Object> response = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity<>(credentials),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();

            if (response != null && response.containsKey("token")) {
                String token = (String) response.get("token");
                log.info("JWT Token received: {}", token);
                session.setAttribute("jwtToken", token); // Store token in session
                log.info("JWT Token stored in session: {}", session.getAttribute("jwtToken"));
                model.addAttribute("token", token);
                return "redirect:/home";
            } else {
                log.error("Token not received from gateway-service");
                model.addAttribute("errorMessage", "Invalid credentials or access denied");
                return "index";
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Invalid credentials");
            model.addAttribute("errorMessage", "Invalid credentials");
            return "index";
        } catch (Exception e) {
            log.error("Error during login process", e);
            model.addAttribute("errorMessage", "Error occurred while logging in");
            return "index";
        }
    }
}