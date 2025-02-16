package com.bank.gatewayservice.service;

import com.bank.gatewayservice.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilterServiceImpl extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final AccessService accessService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public FilterServiceImpl(@Lazy UserService userService, AccessService accessService, JwtService jwtService,
                             RedisTemplate<String, Object> redisTemplate) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.accessService = accessService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) {
        String token = null;
        try {
            String authHeader = request.getHeader("Authorization");
            token = extractToken(authHeader);
            // check if token is present
            if (token == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("No token provided");
                return;
            }
            // Check if the token is valid
            String username = jwtService.extractUsername(token);
            if (!isTokenValid(token, username)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token");
                return;
            }
            // Check if the user is authenticated
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(request, response, token, username);
            }
            // Token is present, valid and user is authenticated = proceed with filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handleError(response, e);
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtService.extractTokenFromHeader(authHeader);
        }
        return null;
    }

    private boolean isTokenValid(String token, String username) {
        if (username == null) {
            log.warn("Username is null, token: {}", token);
            return false;
        }
        String cachedToken = (String) redisTemplate.opsForValue().get("token:" + username);
        if (cachedToken == null || cachedToken.equals(token)) {
            if (jwtService.isTokenExpired(token)) {
                log.warn("Token expired for user {}: {}", username, token);
                return false;
            }
            return true;
        } else {
            log.warn("Token validation failed for user {}: cached token {} does not match provided token {}",
                    username, cachedToken, token);
            return false;
        }
    }

    private void authenticateUser(HttpServletRequest request, HttpServletResponse response, String token,
                                  String username) throws IOException {
        List<String> roles = jwtService.extractRoles(token);
        User user = userService.findUserByUsername(username);
        if (roles.contains("ROLE_USER") && !roles.contains("ROLE_ADMIN") && !roles.contains("ROLE_MANAGER")) {
            addCustomerNumberToHeader(response, user);
        }
        String targetURI = accessService.extractTargetURI(request);
        accessService.validateRoleAccess(targetURI, roles, response, user);
        if (!response.isCommitted()) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, roles.stream()
                            .map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    // a customer number needed to find what user is it, since every user have their unique customer number
    private void addCustomerNumberToHeader(HttpServletResponse response, User user) {
        if (user != null && user.getCustomerNumber() != null) {
            response.addHeader("X-Customer-Number", user.getCustomerNumber().toString());
        }
    }

    private void handleError(HttpServletResponse response, Exception e) {
        log.error("Error occurred in Authorization Filter: {}", e.getMessage(), e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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