package com.bank.accountservice.strategy.strategies;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.strategy.TransactionUpdateStrategy;
import com.bank.accountservice.util.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NewTransactionCreatedStrategy implements TransactionUpdateStrategy {

    private final BalanceService service;

    @Autowired
    public NewTransactionCreatedStrategy(BalanceService service) {
        this.service = service;
    }

    @Override
    public void execute(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                        Long oldAccountDestinationNumber, Long newAccountDestinationNumber,
                        BigDecimal oldAmount, BigDecimal newAmount,
                        TransactionType oldTransactionType, TransactionType newTransactionType,
                        Long transactionId) throws TransactionProcessingException {

        Account newSourceAccount = service.getAccountFromDatabase(newAccountSourceNumber, transactionId);
        if (newSourceAccount == null) {
            throw new TransactionProcessingException("Could not find an account with id: " + newAccountSourceNumber);
        }

        if (newTransactionType.equals(TransactionType.TRANSFER)) {
            Account newDestinationAccount = service.getAccountFromDatabase(newAccountDestinationNumber, transactionId);
            if (newDestinationAccount == null) {
                throw new TransactionProcessingException("Could not find an account with id: " +
                        newAccountDestinationNumber);
            }
            boolean isTransferMade =
                    service.makeTransfer(newSourceAccount, newDestinationAccount, newAmount, transactionId);
            if (!isTransferMade) {
                throw new TransactionProcessingException("Could not make a transaction with id: " + transactionId);
            }
        } else {
            boolean isBalanceChanged =
                    service.changeBalance(newSourceAccount, newAmount, newTransactionType, transactionId);
            if (!isBalanceChanged) {
                throw new TransactionProcessingException("Could not make a transaction with id: " + transactionId);
            }
        }
    }
}