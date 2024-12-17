package com.bank.webservice.service;

import com.bank.webservice.dto.UserResponse;
import com.bank.webservice.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class FilterServiceImpl extends OncePerRequestFilter implements FilterService {

    private final TokenService tokenService;
    private final HandlerExceptionResolver exceptionResolver;

    @Autowired
    public FilterServiceImpl(TokenService tokenService,
                             @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.tokenService = tokenService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                 FilterChain filterChain) { // errors thrown here handled by Spring default handler
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String token = (String) session.getAttribute("jwtToken");
                if (token != null) {
                    try {
                        // Include original URI as a custom header. It is needed for access control.
                        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                            @Override
                            public String getHeader(String name) {
                                if ("X-Original-URI".equals(name)) {
                                    return request.getRequestURI();
                                }
                                return super.getHeader(name);
                            }
                        };
                        // Log to ensure the custom header is included
                        log.info("Adding X-Original-URI header with value: {}",
                                wrappedRequest.getHeader("X-Original-URI"));

                        log.info("Request headers: {}", Collections.list(request.getHeaderNames()));

                        UserResponse userResponse = tokenService.validateToken(token);
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
                    } catch (UnauthorizedException e) {
                        // Catch UnauthorizedException and redirect to access-denied page
                        log.warn("Access denied: {}", e.getMessage());
                        response.sendRedirect("/access-denied");
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
        } catch (Exception e) {
            log.error("Error occurred in Authorization Filter: {}", e.getMessage(), e);
            // to forward the exception to GlobalExceptionHandler class
            exceptionResolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.info("Filtering request for path: {}", path);

        // Skip filtering for certain pages, and resources, so there will be no several requests instead of one
        return path.equals("/index") || path.equals("/access-denied") || path.equals("/error") || path.equals("/login")
                || path.equals("/register") || path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".ico")
                || path.endsWith(".html");
    }
}