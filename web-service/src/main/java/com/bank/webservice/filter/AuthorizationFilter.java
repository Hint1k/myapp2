package com.bank.webservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            String token = (String) session.getAttribute("jwtToken");
            if (token != null) {
                request.setAttribute("Authorization", "Bearer " + token);
                log.info("Authorization header set for request: Bearer {}", token);
            } else {
                log.info("No token found in session, Authorization header not set.");
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.info("Filtering request for path: {}", path);  // Add logging here
        return path.startsWith("/static/") || path.endsWith(".css") || path.endsWith(".js");
    }
}