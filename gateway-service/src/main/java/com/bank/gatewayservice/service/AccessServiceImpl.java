package com.bank.gatewayservice.service;

import com.bank.gatewayservice.cache.AccountCache;
import com.bank.gatewayservice.cache.CustomerCache;
import com.bank.gatewayservice.cache.TransactionCache;
import com.bank.gatewayservice.dto.Account;
import com.bank.gatewayservice.dto.Customer;
import com.bank.gatewayservice.dto.Transaction;
import com.bank.gatewayservice.entity.User;
import com.bank.gatewayservice.util.RestrictedUri;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AccessServiceImpl implements AccessService {

    private final CustomerCache customerCache;
    private final AccountCache accountCache;
    private final TransactionCache transactionCache;

    @Autowired
    public AccessServiceImpl(CustomerCache customerCache, AccountCache accountCache,
                             TransactionCache transactionCache) {
        this.customerCache = customerCache;
        this.accountCache = accountCache;
        this.transactionCache = transactionCache;
    }

    @Override
    public void validateRoleAccess(String requestURI, List<String> roles, HttpServletResponse response, User user)
            throws IOException {
        log.info("Validating access for Request URI: {}", requestURI);
        log.info("User roles: {}", roles);
        log.info("User details: {}", user);

        // Determine the highest priority role
        String highestRole = determineHighestRole(roles);
        if (highestRole == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - No valid role");
            return;
        }

        if ("ROLE_USER".equals(highestRole)) {
            validateUserAccess(requestURI, response, user);
        } else if ("ROLE_MANAGER".equals(highestRole)) {
            validateManagerAccess(requestURI, response);
        }
    }

    private void validateUserAccess(String requestURI, HttpServletResponse response, User user) throws IOException {
        for (RestrictedUri restrictedUri : RestrictedUri.values()) {
            String pattern = convertUriToPattern(restrictedUri.getPath());
            if (requestURI.matches(pattern)) {
                log.info("Matched URI with restricted URI: {}", restrictedUri.getPath());

                if (restrictedUri.isUnconditionallyRestricted()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Restricted URI");
                    return;
                }

                switch (restrictedUri) {
                    case API_CUSTOMERS_ID -> validateCustomerOwnership(requestURI, pattern, response, user);
                    case API_ACCOUNTS_ID -> validateAccountOwnership(requestURI, pattern, response, user);
                    case API_TRANSACTIONS_ID -> validateTransactionOwnership(requestURI, pattern, response, user);
                }
            }
        }
    }

    private void validateManagerAccess(String requestURI, HttpServletResponse response) throws IOException {
        for (RestrictedUri restrictedUri : RestrictedUri.values()) {
            if (restrictedUri.isUnconditionallyRestricted()) {
                String pattern = convertUriToPattern(restrictedUri.getPath());
                if (requestURI.matches(pattern)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Restricted URI");
                    return;
                }
            }
        }
    }

    private void validateCustomerOwnership(String requestURI, String pattern, HttpServletResponse response, User user)
            throws IOException {
        String customerIdString = extractResourceId(requestURI, pattern);
        Long customerId = Long.parseLong(customerIdString);
        Customer customer = customerCache.getCustomerFromCache(customerId);
        if (customer == null || !customer.getCustomerNumber().equals(user.getCustomerNumber())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Customer mismatch");
        }
    }

    private void validateAccountOwnership(String requestURI, String pattern, HttpServletResponse response, User user)
            throws IOException {
        String accountIdString = extractResourceId(requestURI, pattern);
        Long accountId = Long.parseLong(accountIdString);
        Account account = accountCache.getAccountFromCache(accountId);
        if (account == null || !account.getCustomerNumber().equals(user.getCustomerNumber())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Account mismatch");
        }
    }

    private void validateTransactionOwnership(String requestURI, String pattern, HttpServletResponse response,
                                              User user) throws IOException {
        String transactionIdString = extractResourceId(requestURI, pattern);
        Long transactionId = Long.parseLong(transactionIdString);
        Transaction transaction = transactionCache.getTransactionFromCache(transactionId);
        if (transaction != null) {
            Account sourceAccount = accountCache.getAccountFromCache(transaction.getAccountSourceNumber());
            Account destinationAccount = accountCache.getAccountFromCache(transaction.getAccountDestinationNumber());
            if (sourceAccount == null || destinationAccount == null ||
                    !sourceAccount.getCustomerNumber().equals(user.getCustomerNumber()) ||
                    !destinationAccount.getCustomerNumber().equals(user.getCustomerNumber())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Transaction mismatch");
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Transaction not found");
        }
    }

    private String determineHighestRole(List<String> roles) {
        List<String> priorityOrder = List.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER");
        return roles.stream()
                .filter(priorityOrder::contains)
                .min(Comparator.comparingInt(priorityOrder::indexOf))
                .orElse(null);
    }

    private String convertUriToPattern(String path) {
        return path.replaceAll("\\{[^/]+}", "\\\\d+");
    }

    @Override
    public String extractResourceId(String requestURI, String pattern) {
        String regex = pattern.replace("\\d+", "(\\d+)");
        Matcher matcher = Pattern.compile(regex).matcher(requestURI);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Request URI does not match the expected pattern: " + pattern);
    }

    @Override
    public String extractTargetURI(HttpServletRequest request) {
        String originalURI = request.getHeader("X-Original-URI");
        if (originalURI != null) return originalURI;
        String forwardedPath = request.getHeader("X-Forwarded-Path");
        return forwardedPath != null ? forwardedPath : request.getRequestURI();
    }
}