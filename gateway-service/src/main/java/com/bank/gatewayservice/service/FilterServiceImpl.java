package com.bank.gatewayservice.service;

import com.bank.gatewayservice.cache.AccountCache;
import com.bank.gatewayservice.cache.CustomerCache;
import com.bank.gatewayservice.cache.TransactionCache;
import com.bank.gatewayservice.dto.Account;
import com.bank.gatewayservice.dto.Customer;
import com.bank.gatewayservice.dto.Transaction;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilterServiceImpl extends OncePerRequestFilter implements FilterService {

    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HandlerExceptionResolver exceptionResolver;
    private final UserService userService;
    private final CustomerCache customerCache;
    private final AccountCache accountCache;
    private final TransactionCache transactionCache;
    private final List<String> priorityOrder = List.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER");

    @Autowired
    public FilterServiceImpl(JwtService jwtService, RedisTemplate<String, Object> redisTemplate,
                             @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
                             CustomerCache customerCache, AccountCache accountCache, TransactionCache transactionCache,
                             @Lazy UserService userService) { // @Lazy to avoid circular dependency among 3 classes
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.exceptionResolver = exceptionResolver;
        this.userService = userService;
        this.customerCache = customerCache;
        this.accountCache = accountCache;
        this.transactionCache = transactionCache;
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

                    User user = userService.findUserByUsername(username);
                    // Check for ROLE_USER and fetch customerNumber
                    if (roles.contains("ROLE_USER") && !roles.contains("ROLE_ADMIN")
                            && !roles.contains("ROLE_MANAGER")) {
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
                    validateRoleAccess(targetURI, roles, response, user);

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

    private void validateRoleAccess(String requestURI, List<String> roles, HttpServletResponse response, User user)
            throws IOException {
        log.info("Starting validation for Request URI: {}", requestURI);
        log.info("User roles: {}", roles);
        log.info("User details: {}", user);
        log.info("User customerNumber: {}", user.getCustomerNumber());

        // Find the highest priority role
        String highestRole = roles.stream()
                .filter(priorityOrder::contains)
                .min(Comparator.comparingInt(priorityOrder::indexOf))
                .orElse(null);

        log.info("Determined highest role: {}", highestRole);

        // If no valid role is found, deny access
        if (highestRole == null) {
            log.warn("No valid role found for user with roles: {}", roles);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - No valid role");
            return;
        }

        // Apply restrictions only for ROLE_USER
        if ("ROLE_USER".equals(highestRole)) {
            for (RestrictedUri restrictedUri : RestrictedUri.values()) {
                String pattern = convertUriToPattern(restrictedUri.getPath());
                log.info("Checking against RestrictedUri: {}, Pattern: {}", restrictedUri.getPath(), pattern);

                if (requestURI.matches(pattern)) {
                    log.info("Matched URI with restricted URI: {}", restrictedUri.getPath());

                    // Unconditionally restrict certain URIs
                    if (restrictedUri == RestrictedUri.API_CUSTOMERS_NEW ||
                            restrictedUri == RestrictedUri.API_ACCOUNTS_NEW ||
                            restrictedUri == RestrictedUri.API_TRANSACTIONS_NEW) {
                        log.warn("Access denied: Restricted URI {} for ROLE_USER.", requestURI);
                        log.info("Validating URI: {} for Restricted URI: {}", requestURI, restrictedUri.getPath());
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Restricted URI");
                        return;
                    }

                    // Validate ownership for customer-specific URIs
                    if (restrictedUri == RestrictedUri.API_CUSTOMERS_ID) {
                        log.info("Validating URI: {} for Restricted URI: {}", requestURI, restrictedUri.getPath());
                        String customerIdString = extractResourceId(requestURI, pattern);
                        log.info("Fetching customer from cache with ID: {}", customerIdString);
                        Long customerId = Long.parseLong(customerIdString);
                        log.info("Fetching customer ID from cache: {}", customerId);
                        Customer customer = customerCache.getCustomerFromCache(customerId);
                        log.info("Customer details from cache: {}", customer);
                        Long customerNumber = customer.getCustomerNumber();
                        log.info("User's customerNumber: {}", user.getCustomerNumber());

                        if (!customerNumber.equals(user.getCustomerNumber())) {
                            log.warn("Access denied: Customer number mismatch for URI: {} (expected: {}, found: {})",
                                    requestURI, user.getCustomerNumber(), customerId);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "Access Denied - Customer mismatch");
                            return;
                        }
                    } else if (restrictedUri == RestrictedUri.API_ACCOUNTS_ID) {
                        log.info("Validating URI: {} for Restricted URI: {}", requestURI, restrictedUri.getPath());
                        String accountIdString = extractResourceId(requestURI, pattern);
                        log.info("Fetching account from cache with ID: {}", accountIdString);
                        Long accountId = Long.parseLong(accountIdString);
                        log.info("Account id from cache: {}", accountId);
                        Account account = accountCache.getAccountFromCache(accountId);
                        log.info("Account details from cache: {}", account);
                        String customerNumber = account.getCustomerNumber().toString();
                        log.info("User's customerNumber: {}", user.getCustomerNumber());

                        if (!customerNumber.equals(user.getCustomerNumber().toString())) {
                            log.warn("Access denied: Customer Number mismatch for URI: {} (expected: {}, found: {})",
                                    requestURI, user.getCustomerNumber(), accountId);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "Access Denied - Account mismatch");
                            return;
                        }
                    } else if (restrictedUri == RestrictedUri.API_TRANSACTIONS_ID) {
                        log.info("Validating URI: {} for Restricted URI: {}", requestURI, restrictedUri.getPath());
                        String transactionIdString = extractResourceId(requestURI, pattern);
                        log.info("Fetching transaction from cache with ID: {}", transactionIdString);
                        Long transactionId = Long.parseLong(transactionIdString);
                        log.info("Validating ROLE_USER access for transactionId: {}", transactionId);
                        Transaction transaction = null;
                        try {
                            transaction = transactionCache.getTransactionFromCache(transactionId);
                            log.info("Transaction details from cache: {}", transaction);
                        } catch (Exception e) {
                            log.error("Error fetching transaction from cache for ID {}: {}", transactionId,
                                    e.getMessage(), e);
                        }
                        if (transaction != null) {
                            Long accountSourceNumber = transaction.getAccountSourceNumber();
                            Long accountDestinationNumber = transaction.getAccountDestinationNumber();
                            Account sourceAccount = accountCache.getAccountFromCache(accountSourceNumber);
                            log.info("Source Account: {}", sourceAccount);
                            Account destinationAccount = accountCache.getAccountFromCache(accountDestinationNumber);
                            log.info("Destination Account: {}", destinationAccount);
                            Long sourceCustomerNumber = sourceAccount.getCustomerNumber();
                            Long destinationCustomerNumber = destinationAccount.getCustomerNumber();
                            log.info("User's customerNumber: {}", user.getCustomerNumber());

                            if (!sourceCustomerNumber.equals(user.getCustomerNumber()) ||
                                    !destinationCustomerNumber.equals(user.getCustomerNumber())) {
                                log.warn("Access denied: Customer mismatch for URI: {} (expected: {}, found: {})",
                                        requestURI, user.getCustomerNumber(), transactionId);
                                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                        "Access Denied - Transaction mismatch");
                                return;
                            }
                        } else {
                            log.info("Transaction is null");
                        }
                    }

                    // If ownership is validated, grant access
                    log.info("Access granted for user with role: {} on URI: {}", highestRole, requestURI);
                    return;
                }
            }
        }

        // Restrict access for ROLE_MANAGER to only specific restricted URIs in RestrictedUri
        if ("ROLE_MANAGER".equals(highestRole)) {
            for (RestrictedUri restrictedUri : RestrictedUri.values()) {
                if (restrictedUri == RestrictedUri.API_CUSTOMERS_NEW ||
                        restrictedUri == RestrictedUri.API_ACCOUNTS_NEW ||
                        restrictedUri == RestrictedUri.API_TRANSACTIONS_NEW) {
                    String pattern = convertUriToPattern(restrictedUri.getPath());
                    if (requestURI.matches(pattern)) {
                        log.warn("Access denied: Restricted URI {} for ROLE_MANAGER.", requestURI);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Restricted URI");
                        return;
                    }
                }
            }
        }

        // Allow access to non-restricted URIs or for higher roles
        log.info("Request URI: {} is not restricted or user has sufficient privileges. Allowing access.", requestURI);
    }

    private String convertUriToPattern(String path) {
        // Replace {placeholder} with regex to match digits only
        return path.replaceAll("\\{[^/]+}", "\\\\d+"); // Restrict to numeric placeholders
    }

    private String extractResourceId(String requestURI, String pattern) {
        try {
            // Create a regex pattern with a capturing group for the resource ID
            String regex = pattern.replace("\\d+", "(\\d+)");
            Pattern compiledPattern = Pattern.compile(regex);
            Matcher matcher = compiledPattern.matcher(requestURI);

            if (matcher.find()) {
                // Return the captured group (resource ID)
                return matcher.group(1);
            } else {
                throw new IllegalArgumentException("Request URI does not match the expected pattern: " + pattern);
            }
        } catch (Exception e) {
            log.error("Failed to extract resource ID from URI: {} with pattern: {}", requestURI, pattern, e);
            throw e;
        }
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