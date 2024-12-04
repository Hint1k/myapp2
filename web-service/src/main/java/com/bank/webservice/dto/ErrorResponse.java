package com.bank.webservice.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private String details;
}