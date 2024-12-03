package com.bank.webservice.advice;

import com.bank.webservice.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    private final ProxyService proxyService;

    @Autowired
    public GlobalControllerAdvice(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @ModelAttribute
    public void addGlobalAttributes(HttpServletRequest request, Model model) {
        String path = request.getRequestURI();

        // Skip processing for login and public resources
        if (path.equals("/index") || path.equals("/login") ||
                path.startsWith("/static/") ||
                path.endsWith(".css") ||
                path.endsWith(".js")) {
            log.info("Skipping GlobalControllerAdvice for path: {}", path);
            return; // Exit early without processing
        }

        String authHeader = (String) request.getAttribute("Authorization");
        log.info("Authorization header received in GlobalControllerAdvice: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("Authorization header is missing or invalid for the request.");
            model.addAttribute("errorMessage", "Missing or invalid Authorization header");
            return; // Exit early, let the login page load
        }

        String token = authHeader.substring(7);
        log.info("Token received in GlobalControllerAdvice: {}", token);

        try {
            proxyService.addUserInfoToModel(token, model); // Synchronous call
        } catch (Exception e) {
            log.error("Token validation failed", e);
            model.addAttribute("errorMessage", "Token validation failed.");
        }
    }
}