package com.bank.accountservice.service.impl;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.publisher.AccountEventPublisher;
import com.bank.accountservice.publisher.TransactionEventPublisher;
import com.bank.accountservice.service.AccountService;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.service.TransactionService;
import com.bank.accountservice.strategy.*;
import com.bank.accountservice.strategy.strategies.*;
import com.bank.accountservice.util.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionEventPublisher transactionPublisher;
    private final AccountEventPublisher accountPublisher;
    private final TransactionUpdateContext context;
    private final BalanceService balanceService;
    private final AccountService accountService;

    @Override
    @Transactional
    @Synchronized
    public void updateAccountBalanceForTransactionCreate(Long accountSourceNumber, Long accountDestinationNumber,
                                                         BigDecimal amount, TransactionType transactionType,
                                                         Long transactionId) {
        try {
            context.setStrategy(new NewTransactionCreationStrategy(balanceService));
            context.executeStrategy(null, accountSourceNumber, null,
                    accountDestinationNumber, null, amount, null, transactionType,
                    transactionId);
            transactionPublisher.publishTransactionApprovedEvent(transactionId);
        } catch (TransactionProcessingException exception) {
            log.error("Transaction creation failed: {}", exception.getMessage());
            transactionPublisher.publishTransactionFailedEvent(transactionId);
        }
    }

    @Override
    @Transactional
    @Synchronized
    public void updateAccountBalanceForTransactionDelete(Long accountSourceNumber, Long accountDestinationNumber,
                                                         BigDecimal amount, TransactionType transactionType,
                                                         Long transactionId) {
        try {
            context.setStrategy(new DeletingTransactionStrategy(balanceService));
            context.executeStrategy(null, accountSourceNumber, null,
                    accountDestinationNumber, null, amount, null, transactionType,
                    transactionId);

            Account accountSource = accountService.findAccountById(accountSourceNumber);
            accountPublisher.publishAccountDetailsEvent(accountSource);
            if (transactionType == TransactionType.TRANSFER) {
                Account accountDestination = accountService.findAccountById(accountDestinationNumber);
                accountPublisher.publishAccountDetailsEvent(accountDestination);
            }
        } catch (TransactionProcessingException exception) {
            log.error("Transaction deletion failed: {}", exception.getMessage());
            transactionPublisher.publishTransactionFailedEvent(transactionId);
        }
    }

    @Override
    @Transactional
    @Synchronized
    public void updateAccountBalanceForTransactionUpdate(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                                                         Long oldAccountDestinationNumber,
                                                         Long newAccountDestinationNumber,
                                                         BigDecimal oldAmount, BigDecimal newAmount,
                                                         TransactionType oldTransactionType,
                                                         TransactionType newTransactionType, Long transactionId) {

        boolean isOldTransactionATransfer = Objects.equals(oldTransactionType, TransactionType.TRANSFER);
        boolean isNewTransactionATransfer = Objects.equals(newTransactionType, TransactionType.TRANSFER);
        boolean isSourceAccountChanged = !Objects.equals(oldAccountSourceNumber, newAccountSourceNumber);
        boolean isDestinationAccountChanged =
                !Objects.equals(oldAccountDestinationNumber, newAccountDestinationNumber);
        try {
            if (isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged &&
                    !isDestinationAccountChanged) {
                context.setStrategy(new TranToTranSameSrcSameDestStrategy(balanceService));
            }
            if (!isOldTransactionATransfer && !isNewTransactionATransfer && !isSourceAccountChanged) {
                context.setStrategy(new NonTranToNonTranSameSrcStrategy(balanceService));
            }
            if (!isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged) {
                context.setStrategy(new NonTranToTranSameSrcSameDestStrategy(balanceService));
            }
            if (isOldTransactionATransfer && !isNewTransactionATransfer && !isSourceAccountChanged) {
                context.setStrategy(new TranToNonTranSameSrcStrategy(balanceService));
            }
            if (isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged &&
                    isDestinationAccountChanged) {
                context.setStrategy(new TranToTranSameSrcDiffDestStrategy(balanceService));
            }
            if (isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                    isDestinationAccountChanged) {
                context.setStrategy(new TranToTranDiffSrcDiffDestStrategy(balanceService));
            }
            if (!isOldTransactionATransfer && !isNewTransactionATransfer && isSourceAccountChanged) {
                context.setStrategy(new NonTranToNonTranDiffSrcStrategy(balanceService));
            }
            if (!isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged) {
                context.setStrategy(new NonTranToTranDiffSrcStrategy(balanceService));
            }
            if (isOldTransactionATransfer && !isNewTransactionATransfer && isSourceAccountChanged) {
                context.setStrategy(new TranToNonTranDiffSrcStrategy(balanceService));
            }
            if (isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                    !isDestinationAccountChanged) {
                context.setStrategy(new TranToTranDiffSrcSameDestStrategy(balanceService));
            }
            if (!isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                    !isDestinationAccountChanged) {
                context.setStrategy(new NonTranToTranDiffSrcSameDestStrategy(balanceService));
            }

            context.executeStrategy(oldAccountSourceNumber, newAccountSourceNumber,
                    oldAccountDestinationNumber, newAccountDestinationNumber,
                    oldAmount, newAmount, oldTransactionType, newTransactionType,
                    transactionId);
            transactionPublisher.publishTransactionApprovedEvent(transactionId);
        } catch (TransactionProcessingException exception) {
            log.error("Transaction update failed: {}", exception.getMessage());
            transactionPublisher.publishTransactionFailedEvent(transactionId);
        }
    }
}