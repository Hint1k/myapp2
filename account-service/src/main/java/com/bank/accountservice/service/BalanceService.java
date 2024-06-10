package com.bank.accountservice.service;

import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;

public interface BalanceService {

    void updateAccountBalance(Long accountNumber, BigDecimal amount,
                              Long transactionId, TransactionType transactionType);
}