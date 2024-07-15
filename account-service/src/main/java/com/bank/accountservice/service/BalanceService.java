package com.bank.accountservice.service;

import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;

public interface BalanceService {

    void updateAccountBalanceForCreatedTransaction(Long accountSourceNumber, Long accountDestinationNumber,
                                                   BigDecimal amount, TransactionType transactionType,
                                                   Long transactionId);

    void updateAccountBalanceForUpdatedTransaction(Long oldAccountSourceNumber, Long oldAccountDestinationNumber,
                                                   Long newAccountSourceNumber, Long newAccountDestinationNumber,
                                                   BigDecimal oldAmount, BigDecimal newAmount,
                                                   TransactionType oldTransactionType,
                                                   TransactionType newTransactionType, Long transactionId);
}