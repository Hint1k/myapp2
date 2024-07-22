package com.bank.accountservice.strategy;

import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;

public interface TransactionUpdateContext {

    void setStrategy(TransactionUpdateStrategy strategy);

    void executeStrategy(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                         Long oldAccountDestinationNumber, Long newAccountDestinationNumber,
                         BigDecimal oldAmount, BigDecimal newAmount,
                         TransactionType oldTransactionType, TransactionType newTransactionType,
                         Long transactionId);
}