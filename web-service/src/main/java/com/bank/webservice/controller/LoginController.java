package com.bank.webservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
@Slf4j
public class LoginController {

    private final WebClient webClient;
    private final static String URL = "http://gateway-service:8080/login";

    @Autowired
    public LoginController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestParam String username, @RequestParam String password, Model model) {
        Map<String, String> credentials = Map.of("username", username, "password", password);

        return webClient.post()
                .uri(URL)
                .body(BodyInserters.fromValue(credentials))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.error(new RuntimeException("Invalid credentials"));
                    }
                    return Mono.error(new RuntimeException("Unexpected client error occurred."));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new RuntimeException("An internal server error occurred. Please try again.")))
                .bodyToMono(String.class)
                .flatMap(response -> {
                    model.addAttribute("token", response);
                    return Mono.just("redirect:/index");
                })
                .onErrorResume(RuntimeException.class, ex -> {
                    model.addAttribute("errorMessage", ex.getMessage());
                    return Mono.just("gateway/login");
                });
    }
}