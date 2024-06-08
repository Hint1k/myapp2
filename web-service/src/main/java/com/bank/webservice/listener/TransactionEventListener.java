package com.bank.webservice.listener;

import com.bank.webservice.cache.TransactionCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.transaction.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TransactionEventListener {

    private final TransactionCache cache;

    @Autowired
    public TransactionEventListener(TransactionCache cache) {
        this.cache = cache;
    }

    @KafkaListener(topics = "transaction-created", groupId = "web-service")
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event, Acknowledgment acknowledgment) {
        Transaction transaction = new Transaction(
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionTime(),
                event.getTransactionType(),
                event.getAccountDestinationNumber()
        );
        log.info("Received transaction-created event for transaction id: {}", event.getTransactionId());
        cache.addTransactionToCache(transaction.getTransactionId(), transaction);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "transaction-updated", groupId = "web-service")
    public void handleTransactionUpdatedEvent(TransactionUpdatedEvent event, Acknowledgment acknowledgment) {
        Transaction transaction = new Transaction(
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionTime(),
                event.getTransactionType(),
                event.getAccountDestinationNumber()
        );
        Long transactionId = event.getTransactionId();
        log.info("Received transaction-updated event for transaction id: {}", transactionId);
        cache.updateTransactionFromCache(transactionId, transaction);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "transaction-deleted", groupId = "web-service")
    public void handleTransactionDeletedEvent(TransactionDeletedEvent event, Acknowledgment acknowledgment) {
        Long transactionId = event.getTransactionId();
        log.info("Received transaction-deleted event for transaction id: {}", transactionId);
        cache.deleteTransactionFromCache(transactionId);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "all-transactions-received", groupId = "web-service")
    public void handleAllTransactionsEvent(AllTransactionsEvent event, Acknowledgment acknowledgment) {
        List<Transaction> transactions = event.getTransactions();
        log.info("Received all-transactions-received event with {} transactions", transactions.size());
        cache.addAllTransactionsToCache(transactions);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "transaction-details-received", groupId = "web-service")
    public void handleTransactionDetailsEvent(TransactionDetailsEvent event, Acknowledgment acknowledgment) {
        Transaction transaction = new Transaction(
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionTime(),
                event.getTransactionType(),
                event.getAccountDestinationNumber()
        );
        log.info("Received transaction-details-received event for transaction id: {}", event.getTransactionId());
        cache.addTransactionToCache(transaction.getTransactionId(), transaction);
        acknowledgment.acknowledge();
    }
}