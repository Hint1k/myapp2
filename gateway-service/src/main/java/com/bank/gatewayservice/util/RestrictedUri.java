package com.bank.gatewayservice.util;

import lombok.Getter;

@Getter
public enum RestrictedUri {
    API_CUSTOMERS_NEW("/api/customers/new-customer"),
    API_CUSTOMERS_ID("/api/customers/{customerId}"),
    API_ACCOUNTS_NEW("/api/accounts/new-account"),
    API_ACCOUNTS_ID("/api/accounts/{accountId}"),
    API_TRANSACTIONS_NEW("/api/transactions/new-transaction"),
    API_TRANSACTIONS_ID("/api/transactions/{transactionId}");

    private final String path;

    RestrictedUri(String path) {
        this.path = path;
    }
}