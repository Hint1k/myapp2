package com.bank.accountservice.strategy;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.util.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NonTranToTranSameSrcSameDestStrategy implements TransactionUpdateStrategy {

    private final BalanceService service;

    @Autowired
    public NonTranToTranSameSrcSameDestStrategy(BalanceService service) {
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
        Account newDestinationAccount = service.getAccountFromDatabase(newAccountDestinationNumber, transactionId);
        if (newDestinationAccount == null) {
            throw new TransactionProcessingException("Could not find an account with id: " +
                    newAccountDestinationNumber);
        }

        boolean isBalanceReversed =
                service.reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId);
        if (!isBalanceReversed) {
            throw new TransactionProcessingException("Could not reverse a transaction with id: " + transactionId);
        }
        boolean isTransferMade =
                service.makeTransfer(oldSourceAccount, newDestinationAccount, newAmount, transactionId);
        if (!isTransferMade) {
            throw new TransactionProcessingException("Could not make a transaction with id: " + transactionId);
        }
    }
}