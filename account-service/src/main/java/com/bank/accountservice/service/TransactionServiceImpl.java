package com.bank.accountservice.service;

import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.publisher.TransactionEventPublisher;
import com.bank.accountservice.strategy.*;
import com.bank.accountservice.util.TransactionType;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionEventPublisher publisher;
    private final TransactionUpdateContext context;
    private final BalanceService service;

    @Autowired
    public TransactionServiceImpl(TransactionEventPublisher publisher, TransactionUpdateContext context,
                                  BalanceService service) {
        this.publisher = publisher;
        this.context = context;
        this.service = service;
    }

    @Override
    @Transactional
    @Synchronized // TODO implement optimistic locking with JPA instead
//    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void updateAccountBalanceForTransaction(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                                                   Long oldAccountDestinationNumber,
                                                   Long newAccountDestinationNumber, BigDecimal oldAmount,
                                                   BigDecimal newAmount, TransactionType oldTransactionType,
                                                   TransactionType newTransactionType, Long transactionId) {

        boolean isOldTransactionATransfer = Objects.equals(oldTransactionType, TransactionType.TRANSFER);
        boolean isNewTransactionATransfer = Objects.equals(newTransactionType, TransactionType.TRANSFER);
        boolean isSourceAccountChanged = !Objects.equals(oldAccountSourceNumber, newAccountSourceNumber);
        boolean isDestinationAccountChanged =
                !Objects.equals(oldAccountDestinationNumber, newAccountDestinationNumber);
        try {
            if (oldTransactionType == null) { // new transaction creation strategies
                context.setStrategy(new NewTransactionCreatedStrategy(service));
            } else { // old transaction update strategies
                if (isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged &&
                        !isDestinationAccountChanged) {
                    context.setStrategy(new TranToTranSameSrcSameDestStrategy(service));
                }
                if (!isOldTransactionATransfer && !isNewTransactionATransfer && !isSourceAccountChanged) {
                    context.setStrategy(new NonTranToNonTranSameSrcStrategy(service));
                }
                if (!isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged) {
                    context.setStrategy(new NonTranToTranSameSrcSameDestStrategy(service));
                }
                if (isOldTransactionATransfer && !isNewTransactionATransfer && !isSourceAccountChanged) {
                    context.setStrategy(new TranToNonTranSameSrcStrategy(service));
                }
                if (isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged &&
                        isDestinationAccountChanged) {
                    context.setStrategy(new TranToTranSameSrcDiffDestStrategy(service));
                }
                if (isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                        isDestinationAccountChanged) {
                    context.setStrategy(new TranToTranDiffSrcDiffDestStrategy(service));
                }
                if (!isOldTransactionATransfer && !isNewTransactionATransfer && isSourceAccountChanged) {
                    context.setStrategy(new NonTranToNonTranDiffSrcStrategy(service));
                }
                if (!isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged) {
                    context.setStrategy(new NonTranToTranDiffSrcStrategy(service));
                }
                if (isOldTransactionATransfer && !isNewTransactionATransfer && isSourceAccountChanged) {
                    context.setStrategy(new TranToNonTranDiffSrcStrategy(service));
                }
                if (isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                        !isDestinationAccountChanged) {
                    context.setStrategy(new TranToTranDiffSrcSameDestStrategy(service));
                }
                if (!isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                        !isDestinationAccountChanged) {
                    context.setStrategy(new NonTranToTranDiffSrcSameDestStrategy(service));
                }
            }
            context.executeStrategy(oldAccountSourceNumber, newAccountSourceNumber,
                    oldAccountDestinationNumber, newAccountDestinationNumber,
                    oldAmount, newAmount, oldTransactionType, newTransactionType,
                    transactionId);
            publisher.publishTransactionApprovedEvent(transactionId);
        } catch (TransactionProcessingException exception) {
            log.error("Transaction failed: {}", exception.getMessage());
            publisher.publishTransactionFailedEvent(transactionId);
        }
    }
}