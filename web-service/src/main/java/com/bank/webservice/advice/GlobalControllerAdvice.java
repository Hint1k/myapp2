package com.bank.webservice.advice;

import com.bank.webservice.service.ProxyService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.async.DeferredResult;

@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    private final ProxyService proxyService;

    @Autowired
    public GlobalControllerAdvice(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @PostConstruct
    public void init() {
        log.info("GlobalControllerAdvice has been initialized");
    }

    @ModelAttribute
    public DeferredResult<Void> addGlobalAttributes(HttpServletRequest request, Model model) {
        String authHeader = (String) request.getAttribute("Authorization");
        log.info("Authorization header received in GlobalControllerAdvice: {}", authHeader);

        DeferredResult<Void> result = new DeferredResult<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("Authorization header is missing or invalid for the request.");
            model.addAttribute("errorMessage", "Missing or invalid Authorization header");
            result.setResult(null); // Complete the DeferredResult early
            return result;
        }

        String token = authHeader.substring(7);
        log.info("Token received in GlobalControllerAdvice: {}", token);

        proxyService.addUserInfoToModel(token, model)
                .doOnError(e -> {
                    log.error("Error during token validation", e);
                    model.addAttribute("errorMessage", "Token validation failed.");
                    result.setResult(null);
                })
                .doOnSuccess(unused -> result.setResult(null))
                .subscribe();

        return result;
    }
}