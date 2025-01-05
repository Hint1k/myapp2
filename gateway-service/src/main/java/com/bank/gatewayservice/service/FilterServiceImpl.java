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
        try {
            String authHeader = request.getHeader("Authorization");
            String token = extractToken(authHeader);
            String username = token != null ? jwtService.extractUsername(token) : null;
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                handleAuthentication(request, response, token, username);
            }
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

    private void handleAuthentication(HttpServletRequest request, HttpServletResponse response, String token,
                                      String username) throws IOException {
        String cachedToken = (String) redisTemplate.opsForValue().get("token:" + username);
        if (cachedToken != null && cachedToken.equals(token)) {
            if (jwtService.isTokenExpired(token)) {
                log.warn("Token expired for user {}: {}", username, token);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            authenticateUser(request, response, token, username);
        } else {
            log.warn("Token validation failed for user {} or token mismatch", username);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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

    private void addCustomerNumberToHeader(HttpServletResponse response, User user) {
        if (user != null && user.getCustomerNumber() != null) {
            response.addHeader("X-Customer-Number", user.getCustomerNumber().toString());
        }
    }

    private void handleError(HttpServletResponse response, Exception e) {
        log.error("Error occurred in Authorization Filter: {}", e.getMessage(), e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}