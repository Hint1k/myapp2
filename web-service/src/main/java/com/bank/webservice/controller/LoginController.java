package com.bank.webservice.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
@Slf4j
public class LoginController {

    private final WebClient webClient;
    private static final String URL = "http://gateway-service:8080/login";

    @Autowired
    public LoginController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestParam String username, @RequestParam String password, Model model,
                              HttpSession session) {
        Map<String, String> credentials = Map.of("username", username, "password", password);

        return webClient.post().uri(URL).bodyValue(credentials).retrieve().bodyToMono(Map.class)
                .flatMap(response -> {
                    String token = (String) response.get("token");
                    log.info("JWT Token received: {}", token);
                    session.setAttribute("jwtToken", token); // Store token in session
                    log.info("JWT Token stored in session: {}", session.getAttribute("jwtToken"));
                    model.addAttribute("token", token);
                    return Mono.just("redirect:/home");
                })
                .onErrorResume(e -> {
                    log.error("Error during login process", e);
                    model.addAttribute("errorMessage", "Invalid credentials or access denied.");
                    return Mono.just("index");
                });
    }
}