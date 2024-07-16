package com.bank.accountservice.service.strategy;

import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;

public interface TransactionStrategy {

    void execute(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                 Long oldAccountDestinationNumber, Long newAccountDestinationNumber,
                 BigDecimal oldAmount, BigDecimal newAmount,
                 TransactionType oldTransactionType, TransactionType newTransactionType,
                 Long transactionId);
}