package com.bank.webservice.util;

public enum TransactionStatus {
    APPROVED,
    FAILED,
    PENDING,
    FROZEN, // if account is deleted - all the linked transactions are frozen
    SUSPENDED // if account is not active - all the linked transactions are suspended
}