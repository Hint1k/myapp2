package com.bank.webservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private String username;
    private List<String> roles;
}