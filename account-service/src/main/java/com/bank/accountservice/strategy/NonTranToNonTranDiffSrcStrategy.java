package com.bank.accountservice.strategy;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.util.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NonTranToNonTranDiffSrcStrategy implements TransactionUpdateStrategy {

    private final BalanceService service;

    @Autowired
    public NonTranToNonTranDiffSrcStrategy(BalanceService service) {
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
        Account newSourceAccount = service.getAccountFromDatabase(newAccountSourceNumber, transactionId);
        if (newSourceAccount == null) {
            throw new TransactionProcessingException("Could not find an account with id: " + newAccountSourceNumber);
        }

        boolean isBalanceReversed =
                service.reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId);
        if (!isBalanceReversed) {
            throw new TransactionProcessingException("Could not reverse a transaction with id: " + transactionId);
        }
        boolean isBalanceChanged =
                service.changeBalance(newSourceAccount, newAmount, newTransactionType, transactionId);
        if (!isBalanceChanged) {
            throw new TransactionProcessingException("Could not make a transaction with id: " + transactionId);
        }
    }
}