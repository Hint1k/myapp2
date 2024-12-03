package com.bank.webservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    private final RestTemplate restTemplate; // Synchronous RestTemplate to communicate with gateway-service
    private static final String VERIFY_URL = "http://gateway-service:8080/verify"; // Verification endpoint

    @Autowired
    public AuthorizationFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Skip filtering for the login page and index page
        if (path.equals("/index") || path.equals("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            String token = (String) session.getAttribute("jwtToken");
            if (token != null) {
                // Set the Authorization header
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // Synchronous call to verify token validity
                try {
                    restTemplate.exchange(VERIFY_URL, HttpMethod.GET, entity, Void.class);
                    request.setAttribute("Authorization", "Bearer " + token);
                    log.info("Authorization header set for request: Bearer {}", token);
                } catch (Exception e) {
                    log.warn("Token validation failed with gateway-service: {}", e.getMessage());
                    response.sendRedirect("/index"); // Redirect to login if token is invalid
                    return;
                }
            } else {
                log.info("No token found in session, redirecting to login page.");
                response.sendRedirect("/index");
                return;
            }
        } else {
            log.info("No session found, redirecting to login page.");
            response.sendRedirect("/index");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.info("Filtering request for path: {}", path);

        // Skip filtering for login page, index page, and static resources
        return path.equals("/index") || path.equals("/login") || path.startsWith("/static/") ||
                path.endsWith(".css") || path.endsWith(".js");
    }
}