package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Transaction;

import java.util.List;

public interface TransactionService {

    void saveTransaction(Transaction transaction);

    void updateTransaction(Transaction transaction);

    void deleteTransaction(Long transactionId);

    List<Transaction> findAllTransactions();

    Transaction findTransactionById(Long transactionId);

    void handleTransactionFailure(Long transactionId);

    void handleTransactionApproval(Long transactionId);
}