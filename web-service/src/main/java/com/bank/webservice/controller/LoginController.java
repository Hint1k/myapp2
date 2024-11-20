package com.bank.webservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@Slf4j
public class LoginController {

    private final WebClient webClient;
    private static final String AUTH_URL = "http://gateway-service:8080/login";
    private static final String VERIFY_URL = "http://gateway-service:8080/verify";

    @Autowired
    public LoginController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestParam String username, @RequestParam String password, Model model) {
        Map<String, String> credentials = Map.of("username", username, "password", password);

        return webClient.post().uri(AUTH_URL).bodyValue(credentials).retrieve().bodyToMono(Map.class)
                .flatMap(response -> {
                    String token = (String) response.get("token");
                    // Call gateway-service to verify roles
                    return webClient.get()
                            .uri(VERIFY_URL)
                            .header("Authorization", "Bearer " + token)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .flatMap(verificationResponse -> {
                                 // handling unchecked cast
                                Optional<Object> rolesObject = Optional.ofNullable(verificationResponse.get("roles"));
                                List<String> roles = rolesObject.filter(List.class::isInstance)
                                        .map(obj -> (List<?>) obj) // Cast to List<?> safely
                                        .map(list -> list.stream()
                                                .filter(String.class::isInstance)
                                                .map(String.class::cast)
                                                .toList()
                                        )
                                        .orElseGet(() -> {
                                            log.warn("Roles are either null or not a list of strings");
                                            return List.of(); // Default to empty list if roles are invalid
                                        });

                                // Store roles and token in the session or model
                                model.addAttribute("token", token);
                                model.addAttribute("roles", roles);

                                // Redirect based on roles
                                if (roles.contains("ROLE_ADMIN")) {
                                    return Mono.just("redirect:/admin/index");
                                } else if (roles.contains("ROLE_MANAGER")) {
                                    return Mono.just("redirect:/manager/index");
                                } else if (roles.contains("ROLE_USER")) {
                                    return Mono.just("redirect:/user/index");
                                } else {
                                    return Mono.just("redirect:/access-denied");
                                }
                            });
                })
                .onErrorResume(e -> {
                    model.addAttribute("errorMessage",
                            "Invalid credentials or access denied.");
                    return Mono.just("gateway/login");
                });
    }
}