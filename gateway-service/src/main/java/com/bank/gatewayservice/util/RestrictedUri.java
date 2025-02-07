package com.bank.gatewayservice.util;

import lombok.Getter;

@Getter
public enum RestrictedUri {
    API_CUSTOMERS_NEW("/api/customers/new-customer", true),
    API_CUSTOMERS_ID("/api/customers/{customerId}", false),
    API_ACCOUNTS_NEW("/api/accounts/new-account", true),
    API_ACCOUNTS_ID("/api/accounts/{accountId}", false),
    API_TRANSACTIONS_NEW("/api/transactions/new-transaction", true),
    API_TRANSACTIONS_ID("/api/transactions/{transactionId}", false),
    API_CUSTOMERS_ALL("/api/customers/all-customers/{customerId}", true),
    API_ACCOUNTS_ALL("/api/accounts/all-accounts/{accountId}", false),
    API_TRANSACTIONS_ALL("/api/transactions/all-transactions/{transactionId}", false);

    private final String path;
    private final boolean unconditionallyRestricted;

    RestrictedUri(String path, boolean unconditionallyRestricted) {
        this.path = path;
        this.unconditionallyRestricted = unconditionallyRestricted;
    }
}