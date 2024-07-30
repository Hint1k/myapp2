package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.util.AccountStatus;

import java.util.List;

public interface TransactionService {

    void saveTransaction(Transaction transaction);

    void updateTransaction(Transaction transaction);

    void deleteTransaction(Long transactionId);

    List<Transaction> findAllTransactions();

    Transaction findTransactionById(Long transactionId);

    void handleTransactionFailure(Long transactionId);

    void handleTransactionApproval(Long transactionId);

    void freezeTransactions(Long accountNumber);

    void suspendOrUnsuspendTransactions(Long accountNumber, String suspend);
}