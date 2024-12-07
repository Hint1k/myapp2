package com.bank.gatewayservice.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FilterService {

    void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                          FilterChain filterChain);
}