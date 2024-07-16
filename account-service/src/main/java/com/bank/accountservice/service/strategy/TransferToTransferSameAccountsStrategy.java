package com.bank.accountservice.service.strategy;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.util.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferToTransferSameAccountsStrategy implements TransactionStrategy {

    @Autowired
    private Balance balance;

    @Override
    public void execute(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                        Long oldAccountDestinationNumber, Long newAccountDestinationNumber,
                        BigDecimal oldAmount, BigDecimal newAmount, TransactionType oldTransactionType,
                        TransactionType newTransactionType, Long transactionId) {

        Account oldSourceAccount = balance.getAccountFromDatabase(oldAccountSourceNumber, transactionId);
        Account oldDestinationAccount = balance.getAccountFromDatabase(oldAccountDestinationNumber, transactionId);

        if (oldSourceAccount != null && oldDestinationAccount != null) {
            boolean isTransferReversed = balance.reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
            if (!isTransferReversed) {

            }
            boolean isTransferMade = balance.makeTransfer(oldSourceAccount, oldDestinationAccount, newAmount, transactionId);
            if (!isTransferMade) {

            }
        } else {

        }
    }
}