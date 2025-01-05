package com.bank.gatewayservice.service;

import com.bank.gatewayservice.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface AccessService {

    void validateRoleAccess(String requestURI, List<String> roles, HttpServletResponse response, User user)
            throws IOException;

    String extractResourceId(String requestURI, String pattern);

    String extractTargetURI(HttpServletRequest request);
}