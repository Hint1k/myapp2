package com.bank.gatewayservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/proxy")
public class ProxyController {

    private final WebClient webClient;
    private final static String LOGIN_URL = "http://web-service:8080/login";
    private final static String INDEX_URL = "http://web-service:8080/index";

    @Autowired
    public ProxyController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody Map<String, String> body) {
        return webClient.post()
                .uri(LOGIN_URL)
                .bodyValue(body)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
    }

    @GetMapping("/index")
    public Mono<ResponseEntity<String>> index(HttpServletRequest request) {
        // The JwtFilter ensures that only users with ROLE_ADMIN can reach this point.
        return webClient.get()
                .uri(INDEX_URL)
                .headers(headers -> {
                    String token = request.getHeader("Authorization");
                    if (token != null) {
                        headers.set("Authorization", token);
                    }
                })
                .retrieve()
                .toEntity(String.class);
    }
}