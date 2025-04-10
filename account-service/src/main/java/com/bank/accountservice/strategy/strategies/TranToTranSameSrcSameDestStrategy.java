package com.bank.accountservice.strategy.strategies;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.strategy.TransactionUpdateStrategy;
import com.bank.accountservice.util.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.bank.accountservice.exception.TransactionProcessingException;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TranToTranSameSrcSameDestStrategy implements TransactionUpdateStrategy {

    private final BalanceService service;

    @Override
    public void execute(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                        Long oldAccountDestinationNumber, Long newAccountDestinationNumber,
                        BigDecimal oldAmount, BigDecimal newAmount, TransactionType oldTransactionType,
                        TransactionType newTransactionType, Long transactionId) throws TransactionProcessingException {

        Account oldSourceAccount = service.getAccountFromDatabase(oldAccountSourceNumber, transactionId);
        if (oldSourceAccount == null) {
            throw new TransactionProcessingException("Could not find an account with id: " + oldAccountSourceNumber);
        }
        Account oldDestinationAccount = service.getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
        if (oldDestinationAccount == null) {
            throw new TransactionProcessingException("Could not find an account with id: " +
                    oldAccountDestinationNumber);
        }

        boolean isTransferReversed =
                service.reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
        if (!isTransferReversed) {
            throw new TransactionProcessingException("Could not reverse a transaction with id: " + transactionId);
        }
        boolean isTransferMade =
                service.makeTransfer(oldSourceAccount, oldDestinationAccount, newAmount, transactionId);
        if (!isTransferMade) {
            throw new TransactionProcessingException("Could not make a transaction with id: " + transactionId);
        }
    }
}