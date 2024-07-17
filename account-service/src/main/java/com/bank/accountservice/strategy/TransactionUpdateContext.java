package com.bank.accountservice.strategy;

import com.bank.accountservice.util.TransactionType;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Setter
public class TransactionUpdateContext {

    private TransactionUpdateStrategy strategy;

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