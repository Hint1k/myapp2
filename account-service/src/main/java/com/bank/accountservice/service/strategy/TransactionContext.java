package com.bank.accountservice.service.strategy;

import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;

public class TransactionContext {

    private TransactionStrategy strategy;

    public void setStrategy(TransactionStrategy strategy) {
        this.strategy = strategy;
    }

    public void executeStrategy(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                                Long oldAccountDestinationNumber, Long newAccountDestinationNumber,
                                BigDecimal oldAmount, BigDecimal newAmount,
                                TransactionType oldTransactionType, TransactionType newTransactionType,
                                Long transactionId) {
        strategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                oldAccountDestinationNumber, newAccountDestinationNumber,
                oldAmount, newAmount, oldTransactionType, newTransactionType,
                transactionId);
    }

}