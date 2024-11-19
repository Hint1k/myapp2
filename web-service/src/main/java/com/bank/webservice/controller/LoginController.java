package com.bank.webservice.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

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
    public Mono<String> login(@RequestParam String username, @RequestParam String password, Model model,
                              HttpSession session) {

        Map<String, String> credentials = Map.of("username", username, "password", password);

        return webClient.post()
                .uri(AUTH_URL)
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
                .flatMap(token -> {
                    // Store the token in the session
                    session.setAttribute("jwtToken", token);
                    return Mono.just("redirect:/index");
                })
                .onErrorResume(RuntimeException.class, ex -> {
                    model.addAttribute("errorMessage", ex.getMessage());
                    return Mono.just("gateway/login");
                });
    }

    @GetMapping("/index")
    public Mono<String> showIndex(HttpSession session, Model model) {
        String token = (String) session.getAttribute("jwtToken");
        if (token == null) {
            return Mono.just("redirect:/login");
        }

//        return webClient.get()
//                .uri(VERIFY_URL)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
//                    if (clientResponse.statusCode() == HttpStatus.FORBIDDEN) {
//                        return Mono.error(new RuntimeException("Access Denied"));
//                    }
//                    return Mono.error(new RuntimeException("Unexpected client error occurred."));
//                })
//                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
//                        Mono.error(new RuntimeException("An internal server error occurred. Please try again.")))
//                .bodyToMono(String.class)
//                .flatMap(response -> Mono.just("index")) // Authorized
//                .onErrorResume(RuntimeException.class, ex -> {
//                    if ("Access Denied".equals(ex.getMessage())) {
//                        return Mono.just("access-denied");
//                    }
//                    return Mono.just("redirect:/login");
//                });

        return webClient.get()
                .uri(VERIFY_URL)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    List<String> roles = (List<String>) response.get("roles");
                    if (roles.contains("ROLE_ADMIN")) {
                        return Mono.just("index");
                    } else {
                        return Mono.just("access-denied");
                    }
                })
                .onErrorResume(e -> {
                    model.addAttribute("errorMessage", "Token validation failed");
                    return Mono.just("redirect:/login");
                });
    }
}