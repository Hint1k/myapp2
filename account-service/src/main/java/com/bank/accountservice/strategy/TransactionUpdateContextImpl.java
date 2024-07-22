package com.bank.accountservice.strategy;

import com.bank.accountservice.util.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionUpdateContextImpl implements TransactionUpdateContext {

    private TransactionUpdateStrategy strategy;

    @Override
    public void setStrategy(TransactionUpdateStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
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