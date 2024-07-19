package com.bank.accountservice.service;

import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;

public interface TransactionService {

    void updateAccountBalanceForTransactionCreate(Long accountSourceNumber, Long accountDestinationNumber,
                                                  BigDecimal amount, TransactionType transactionType,
                                                  Long transactionId);

    void updateAccountBalanceForTransactionDelete(Long accountSourceNumber, Long accountDestinationNumber,
                                                  BigDecimal amount, TransactionType transactionType,
                                                  Long transactionId);

    void updateAccountBalanceForTransactionUpdate(Long oldAccountSourceNumber, Long oldAccountDestinationNumber,
                                                  Long newAccountSourceNumber, Long newAccountDestinationNumber,
                                                  BigDecimal oldAmount, BigDecimal newAmount,
                                                  TransactionType oldTransactionType,
                                                  TransactionType newTransactionType, Long transactionId);
}