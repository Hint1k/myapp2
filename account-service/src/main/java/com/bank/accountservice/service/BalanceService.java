package com.bank.accountservice.service;

import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;

public interface BalanceService {

    void updateAccountBalanceForCreatedTransaction(Long accountSourceNumber, Long accountDestinationNumber,
                                                   BigDecimal amount, Long transactionId,
                                                   TransactionType transactionType);

    void updateAccountBalanceForUpdatedTransaction(Long accountSourceNumber, Long accountDestinationNumber,
                                                   BigDecimal oldAmount, BigDecimal newAmount, Long transactionId,
                                                   TransactionType oldTransactionType,
                                                   TransactionType newTransactionType);
}