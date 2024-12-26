package com.bank.webservice.cache;

import com.bank.webservice.dto.Transaction;

import java.util.List;

public interface TransactionCache {

    void addTransactionToCache(Long transactionId, Transaction transaction);

    void addAllTransactionsToCache(List<Transaction> transactions);

    void updateTransactionFromCache(Long transactionId, Transaction transaction);

    void deleteTransactionFromCache(Long transactionId);

    List<Transaction> getAllTransactionsFromCache();

    Transaction getTransactionFromCache(Long transactionId);

    List<Transaction> getTransactionsForAccountFromCache(Long accountNumber);

    List<Transaction> getTransactionsForMultipleAccountsFromCache(List<Long> accountNumbers);
}