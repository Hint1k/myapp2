package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Transaction;

import java.util.List;

public interface TransactionCache {

    List<Transaction> getAllTransactionsFromCache();

    Transaction getTransactionFromCache(Long transactionId);

    List<Transaction> getTransactionsForAccountFromCache(Long accountNumber);

    List<Transaction> getTransactionsForMultipleAccountsFromCache(List<Long> accountNumbers);
}