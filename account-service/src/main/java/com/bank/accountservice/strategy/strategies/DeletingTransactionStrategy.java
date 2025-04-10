package com.bank.accountservice.strategy.strategies;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.strategy.TransactionUpdateStrategy;
import com.bank.accountservice.util.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DeletingTransactionStrategy implements TransactionUpdateStrategy {

    private final BalanceService service;

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
            boolean isTransferReversed =
                    service.reverseTransfer(newSourceAccount, newDestinationAccount, newAmount, transactionId);
            if (!isTransferReversed) {
                throw new TransactionProcessingException("Could not make a transaction with id: " + transactionId);
            }
        } else {
            boolean isBalanceReversed =
                    service.reverseBalance(newSourceAccount, newAmount, newTransactionType, transactionId);
            if (!isBalanceReversed) {
                throw new TransactionProcessingException("Could not make a transaction with id: " + transactionId);
            }
        }
    }
}