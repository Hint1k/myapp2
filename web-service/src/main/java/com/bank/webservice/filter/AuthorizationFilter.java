package com.bank.webservice.filter;

import com.bank.webservice.dto.UserResponse;
import com.bank.webservice.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    private final TokenService proxyService;

    @Autowired
    public AuthorizationFilter(TokenService proxyService) {
        this.proxyService = proxyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException { // these exceptions handled by GlobalExceptionHandler class
        HttpSession session = request.getSession(false);
        if (session != null) {
            String token = (String) session.getAttribute("jwtToken");
            if (token != null) {
                try {
                    UserResponse userResponse = proxyService.validateToken(token);
                    if (userResponse != null) {
                        String username = userResponse.getUsername();
                        List<String> roles = userResponse.getRoles();
                        request.setAttribute("username", username);
                        request.setAttribute("roles", roles);
                        log.info("Authorization successful: username={}, roles={}", username, roles);
                    } else {
                        log.warn("Token verification response is null. Redirecting to login.");
                        response.sendRedirect("/index");
                        return;
                    }
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

        // Skip filtering for login page, index page, and resources so there will be no several requests instead of one
        return path.equals("/index") || path.equals("/login") ||  path.startsWith("/static/") || path.endsWith(".css")
                || path.endsWith(".js") || path.endsWith(".ico") || path.endsWith(".html");
    }
}