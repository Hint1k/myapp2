package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;

public interface BalanceService {

    boolean changeBalance(Account account, BigDecimal amount, TransactionType transactionType, Long transactionId);

    boolean reverseBalance(Account account, BigDecimal amount, TransactionType transactionType, Long transactionId);

    boolean makeTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount, Long transactionId);

    boolean reverseTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount, Long transactionId);

    BigDecimal calculateBalance(BigDecimal currentBalance, BigDecimal amount, TransactionType transactionType);

    boolean hasBalanceProblems(BigDecimal balance, Account account, Long transactionId);

    void saveBalanceToDatabase(Account account, BigDecimal newBalance);

    Account getAccountFromDatabase(Long accountNumber, Long transactionId);

    void handleInsufficientFunds(Account account, Long transactionId);

    void handleWrongTransactionType(Long transactionId);
}