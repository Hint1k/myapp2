package com.bank.webservice.service.impl;

import com.bank.webservice.dto.UserResponse;
import com.bank.webservice.exception.UnauthorizedException;
import com.bank.webservice.service.FilterService;
import com.bank.webservice.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class FilterServiceImpl extends OncePerRequestFilter implements FilterService {

    private final TokenService tokenService;
    private final HandlerExceptionResolver exceptionResolver;

    public FilterServiceImpl(TokenService tokenService,
                             @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.tokenService = tokenService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String token = (String) session.getAttribute("jwtToken");
                if (token != null) {
                    handleTokenValidation(request, response, filterChain, token);
                } else {
                    redirectToLogin(response, "No token found in session");
                }
            } else {
                redirectToLogin(response, "No session found");
            }
        } catch (Exception e) {
            handleError(request, response, e);
        }
    }

    private void handleTokenValidation(HttpServletRequest request, HttpServletResponse response,
                                       FilterChain filterChain, String token) throws IOException, ServletException {
        HttpServletRequest wrappedRequest = wrapRequestWithOriginalURI(request);
        UserResponse userResponse = tokenService.validateToken(token);
        if (userResponse != null) {
            processUserResponse(request, response, userResponse);
        } else {
            redirectToLogin(response, "Token verification response is null");
        }
        filterChain.doFilter(wrappedRequest, response);
    }

    private HttpServletRequest wrapRequestWithOriginalURI(HttpServletRequest request) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("X-Original-URI".equals(name)) {
                    return request.getRequestURI();
                }
                return super.getHeader(name);
            }
        };
    }

    private void processUserResponse(HttpServletRequest request, HttpServletResponse response,
                                     UserResponse userResponse) {
        String username = userResponse.getUsername();
        List<String> roles = userResponse.getRoles();
        request.setAttribute("username", username);
        request.setAttribute("roles", roles);
        log.info("Authorization successful: username={}, roles={}", username, roles);

        if (roles.contains("ROLE_USER")) {
            addCustomerNumberToRequest(request);
        }
    }

    private void addCustomerNumberToRequest(HttpServletRequest request) {
        String customerNumber = (String) request.getAttribute("X-Customer-Number");
        if (customerNumber != null) {
            log.info("ROLE_USER detected with customer number: {}", customerNumber);
            request.setAttribute("customerNumber", customerNumber);
        }
    }

    private void redirectToLogin(HttpServletResponse response, String message) throws IOException {
        log.info("{}; redirecting to login page.", message);
        response.sendRedirect("/index");
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) {
        if (e instanceof UnauthorizedException) {
            try {
                log.warn("Unauthorized access: {}", e.getMessage());
                response.sendRedirect("/access-denied"); // Explicitly redirect to access-denied
            } catch (IOException ioException) {
                log.error("Failed to redirect to access-denied page: {}", ioException.getMessage(), ioException);
                exceptionResolver.resolveException(request, response, null, e);
            }
        } else {
            log.error("Unexpected error occurred in filter: {}", e.getMessage(), e);
            exceptionResolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Check for null or empty path to avoid NullPointerException
        if (path == null || path.isEmpty()) {
            return false;
        }
        // Skip filtering for certain pages, and resources, so there will be no several requests instead of one
        return path.equals("/index") || path.equals("/access-denied") || path.equals("/error") || path.equals("/login")
                || path.equals("/register") || path.equals("/v3/api-docs") || path.equals("/actuator/health")
                || path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".ico") || path.endsWith(".html");
    }
}