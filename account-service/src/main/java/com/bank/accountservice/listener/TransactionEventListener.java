package com.bank.accountservice.listener;

import com.bank.accountservice.event.transaction.TransactionCreatedEvent;
import com.bank.accountservice.event.transaction.TransactionDeletedEvent;
import com.bank.accountservice.event.transaction.TransactionUpdatedEvent;
import com.bank.accountservice.service.TransactionService;
import com.bank.accountservice.util.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class TransactionEventListener {

    private final TransactionService service;

    @Autowired
    public TransactionEventListener(TransactionService service) {
        this.service = service;
    }

    @KafkaListener(topics = "transaction-created", groupId = "account-service")
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received transaction-created event for transaction id: {}", event.getTransactionId());
        Long transactionId = event.getTransactionId();
        BigDecimal amount = event.getAmount();
        TransactionType transactionType = event.getTransactionType();
        Long accountSourceNumber = event.getAccountSourceNumber();
        Long accountDestinationNumber = event.getAccountDestinationNumber();

        service.updateAccountBalanceForTransactionCreate(accountSourceNumber, accountDestinationNumber, amount,
                transactionType, transactionId);

        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "transaction-updated", groupId = "account-service")
    public void handleTransactionUpdatedEvent(TransactionUpdatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received transaction-updated event for transaction id: {}", event.getTransactionId());
        Long transactionId = event.getTransactionId();
        BigDecimal oldAmount = event.getOldAmount();
        BigDecimal newAmount = event.getAmount();
        TransactionType oldTransactionType = event.getOldTransactionType();
        TransactionType newTransactionType = event.getTransactionType();
        Long oldAccountSourceNumber = event.getOldAccountSourceNumber();
        Long newAccountSourceNumber = event.getAccountSourceNumber();
        Long oldAccountDestinationNumber = event.getOldAccountDestinationNumber();
        Long newAccountDestinationNumber = event.getAccountDestinationNumber();

        service.updateAccountBalanceForTransactionUpdate(oldAccountSourceNumber, newAccountSourceNumber,
                oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                oldTransactionType, newTransactionType, transactionId);

        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "transaction-deleted", groupId = "account-service")
    public void handleTransactionDeletedEvent(TransactionDeletedEvent event, Acknowledgment acknowledgment) {
        log.info("Received transaction-deleted event for transaction id: {}", event.getTransactionId());
        Long transactionId = event.getTransactionId();
        BigDecimal amount = event.getAmount();
        TransactionType transactionType = event.getTransactionType();
        Long accountSourceNumber = event.getAccountSourceNumber();
        Long accountDestinationNumber = event.getAccountDestinationNumber();

        service.updateAccountBalanceForTransactionDelete(accountSourceNumber, accountDestinationNumber, amount,
                transactionType, transactionId);

        acknowledgment.acknowledge();
    }
}