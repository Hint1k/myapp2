package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Transaction;

public interface TransactionCache {

    Transaction getTransactionFromCache(Long transactionId);
}