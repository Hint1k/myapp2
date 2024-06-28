package com.bank.accountservice.service;

import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;

public interface BalanceService {

    void updateAccountBalance(Long accountSourceNumber, Long accountDestinationNumber, BigDecimal amount,
                              Long transactionId, TransactionType transactionType);
}