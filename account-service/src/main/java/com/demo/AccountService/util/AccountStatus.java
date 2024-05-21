package com.demo.AccountService.util;

public enum AccountStatus {
    ACTIVE, // The account is currently active and operational
    INACTIVE, // The account is temporarily inactive
    CLOSED, // The account has been closed either by the account holder or by the bank
    LOCKED // The account has been locked, usually due to suspicious activity or security reasons
}