package com.bank.webservice.service;

import com.bank.webservice.dto.UserResponse;

public interface TokenService {

   UserResponse validateToken(String token);
}