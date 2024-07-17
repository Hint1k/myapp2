package com.bank.accountservice.strategy;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.util.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TranToTranSameSrcDiffDestStrategy implements TransactionUpdateStrategy {

    private final BalanceService service;

    @Autowired
    public TranToTranSameSrcDiffDestStrategy(BalanceService service) {
        this.service = service;
    }

    @Override
    public void execute(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                        Long oldAccountDestinationNumber, Long newAccountDestinationNumber,
                        BigDecimal oldAmount, BigDecimal newAmount,
                        TransactionType oldTransactionType, TransactionType newTransactionType,
                        Long transactionId) throws TransactionProcessingException {

        Account oldSourceAccount = service.getAccountFromDatabase(oldAccountSourceNumber, transactionId);
        if (oldSourceAccount == null) {
            throw new TransactionProcessingException("Could not find an account with id: " + oldAccountSourceNumber);
        }
        Account oldDestinationAccount = service.getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
        if (oldDestinationAccount == null) {
            throw new TransactionProcessingException("Could not find an account with id: " +
                    oldAccountDestinationNumber);
        }
        Account newDestinationAccount = service.getAccountFromDatabase(newAccountDestinationNumber, transactionId);
        if (newDestinationAccount == null) {
            throw new TransactionProcessingException("Could not find an account with id: " +
                    newAccountDestinationNumber);
        }

        boolean isTransferReversed =
                service.reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
        if (!isTransferReversed) {
            throw new TransactionProcessingException("Could not reverse a transaction with id: " + transactionId);
        }
        boolean isTransferMade =
                service.makeTransfer(oldSourceAccount, newDestinationAccount, newAmount, transactionId);
        if (!isTransferMade) {
            throw new TransactionProcessingException("Could not make a transaction with id: " + transactionId);
        }
    }
}