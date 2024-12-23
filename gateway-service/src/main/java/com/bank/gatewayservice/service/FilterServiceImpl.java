package com.bank.gatewayservice.service;

import com.bank.gatewayservice.entity.User;
import com.bank.gatewayservice.util.RestrictedUri;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilterServiceImpl extends OncePerRequestFilter implements FilterService {

    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HandlerExceptionResolver exceptionResolver;
    private final UserService userService;
    private final List<String> priorityOrder = List.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER");

    @Autowired
    public FilterServiceImpl(JwtService jwtService, RedisTemplate<String, Object> redisTemplate,
                             @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
                             @Lazy UserService userService) { // @Lazy to avoid circular dependency among 3 classes
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.exceptionResolver = exceptionResolver;
        this.userService = userService;
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) {
        try {
            log.info("JWT filter invoked for URL: {}", request.getRequestURI());

            // Log all headers received
            Enumeration<String> headerNames2 = request.getHeaderNames();
            log.info("Headers received in request:");
            while (headerNames2.hasMoreElements()) {
                String header = headerNames2.nextElement();
                log.info("Header: {} -> {}", header, request.getHeader(header));
            }

            final String authHeader = request.getHeader("Authorization");
            String username = null;
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = jwtService.extractTokenFromHeader(authHeader);
                username = jwtService.extractUsername(token);
                log.info("Extracted token: {} for user: {}", token, username);
            }

            // Check if username is present and token validation is required
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Fetch the token from Redis cache using the key "token:<username>"
                String cachedToken = (String) redisTemplate.opsForValue().get("token:" + username);
                log.info("Fetching cached token from Redis for user {}: {}", username, cachedToken);

                if (cachedToken != null && cachedToken.equals(token)) {
                    // Validate cached token matches the received token
                    if (jwtService.isTokenExpired(token)) {
                        log.warn("Token expired for user {}: {}", username, token);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                    // Token is valid and not expired, authenticate the user
                    List<String> roles = jwtService.extractRoles(token);

                    // Check for ROLE_USER and fetch customerNumber
                    if (roles.contains("ROLE_USER")) {
                        User user = userService.findUserByUsername(username);
                        if (user != null && user.getCustomerNumber() != null) {
                            response.addHeader("X-Customer-Number", user.getCustomerNumber().toString());
                            log.info("Added customer number {} to response header for user {}",
                                    user.getCustomerNumber(), username);
                        }
                    }

                    // Extract actual target URI from the request
                    String targetURI = extractTargetURI(request);
                    log.info("Validating access for target URI: {}", targetURI);

                    // Validate access based on target URI
                    String httpMethod = request.getMethod();
                    validateRoleAccess(targetURI, roles, response, httpMethod);

                    if (response.isCommitted()) {
                        return;
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, roles.stream()
                                    .map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Token validated and user {} authenticated", username);
                } else {
                    log.warn("Token validation failed for user {} or token mismatch", username);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
            // logging the response before proceeding
            response.getHeaderNames().forEach(header ->
                    log.info("Response header: {} -> {}", header, response.getHeader(header))
            );

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error occurred in Authorization Filter: {}", e.getMessage(), e);
            // to forward the exception to GlobalExceptionHandler class
            exceptionResolver.resolveException(request, response, null, e);
        }
    }

    private void validateRoleAccess(String requestURI, List<String> roles, HttpServletResponse response,
                                    String httpMethod) throws IOException {
        log.info("Validating access for Request URI: {}", requestURI);

        // Priority order: ROLE_ADMIN > ROLE_MANAGER > ROLE_USER
        String highestRole = roles.stream().filter(priorityOrder::contains)
                .min(Comparator.comparingInt(priorityOrder::indexOf)).orElse(null);

        if (highestRole == null) {
            log.warn("No valid role found for user with roles: {}", roles);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        log.info("Highest role determined: {}", highestRole);

        // Check access based on highest role and request type
        for (RestrictedUri restrictedUri : RestrictedUri.values()) {
            String pattern = convertUriToPattern(restrictedUri.getPath());
            if (requestURI.equals(restrictedUri.getPath()) || requestURI.matches(pattern)) {
                if ("PUT".equals(httpMethod) && !"ROLE_ADMIN".equals(highestRole)) {
                    log.warn("Access denied for user with role: {} on URL: {}", highestRole, requestURI);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                    return;
                }
                log.info("Access granted for user with role: {} on URI: {}", highestRole, requestURI);
                return;
            }
        }
        log.info("Access granted to user for URI: {}", requestURI);
    }

    private String convertUriToPattern(String path) {
        // Replace {placeholder} with regex to match digits only
        return path.replaceAll("\\{[^/]+}", "\\\\d+"); // Restrict to numeric placeholders
    }

    private String extractTargetURI(HttpServletRequest request) {
        String originalURI = request.getHeader("X-Original-URI"); // Used in reverse proxies
        if (originalURI != null) {
            log.info("Using X-Original-URI: {}", originalURI);
            return originalURI;
        }

        String forwardedPath = request.getHeader("X-Forwarded-Path"); // Another common header
        if (forwardedPath != null) {
            log.info("Using X-Forwarded-Path: {}", forwardedPath);
            return forwardedPath;
        }

        String requestURI = request.getRequestURI();
        log.info("Using Request URI: {}", requestURI);
        return requestURI;
    }
}