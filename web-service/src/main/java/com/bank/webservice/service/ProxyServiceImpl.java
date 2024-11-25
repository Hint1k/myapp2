package com.bank.webservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class ProxyServiceImpl implements ProxyService {

    private final WebClient webClient;
    private static final String VERIFY_URL = "http://gateway-service:8080/verify";

    @Autowired
    public ProxyServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> addUserInfoToModel(String token, Model model) {
        log.info("ProxyService: Validating token");
        log.info("Preparing to send request to VERIFY_URL with Authorization header: Bearer {}", token);

        return webClient.get()
                .uri(VERIFY_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                    log.error("Request failed with status: {}", response.statusCode());
                    return Mono.error(new Exception("Request failed"));
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSubscribe(subscription -> log.info("Subscription triggered for WebClient call"))
                .doOnNext(userInfo -> {
                    log.info("User info retrieved from gateway-service: {}", userInfo);
                    if (userInfo != null) {
                        model.addAttribute("username", userInfo.get("username"));
                        model.addAttribute("roles", userInfo.get("roles"));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error communicating with gateway-service", e);
                    model.addAttribute("errorMessage", "Failed to retrieve user information.");
                    return Mono.empty();
                })
                .doOnTerminate(() -> log.info("Request to VERIFY_URL completed"))
                .then(); // add `then()` here to return Mono<Void>
    }
}