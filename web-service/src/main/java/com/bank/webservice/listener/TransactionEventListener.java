package com.bank.webservice.listener;

import com.bank.webservice.cache.TransactionCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.transaction.*;
import com.bank.webservice.service.LatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class TransactionEventListener {

    private final TransactionCache cache;
    private final LatchService latchService;

    @Autowired
    public TransactionEventListener(TransactionCache cache, LatchService latchService) {
        this.cache = cache;
        this.latchService = latchService;
    }

    @KafkaListener(topics = "transaction-created", groupId = "web-service")
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event, Acknowledgment acknowledgment) {
        Transaction transaction = new Transaction(
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionTime(),
                event.getTransactionType(),
                event.getTransactionStatus(),
                event.getAccountSourceNumber(),
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
                event.getTransactionStatus(),
                event.getAccountSourceNumber(),
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
        CountDownLatch latch = latchService.getLatch();
        if (latch != null) {
            latch.countDown();
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "transaction-details-received", groupId = "web-service")
    public void handleTransactionDetailsEvent(TransactionDetailsEvent event, Acknowledgment acknowledgment) {
        Transaction transaction = new Transaction(
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionTime(),
                event.getTransactionType(),
                event.getTransactionStatus(),
                event.getAccountSourceNumber(),
                event.getAccountDestinationNumber()
        );
        log.info("Received transaction-details-received event for transaction id: {}", event.getTransactionId());
        cache.addTransactionToCache(transaction.getTransactionId(), transaction);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "account-transactions-received", groupId = "web-service")
    public void handleAccountTransactionsEvent(AccountTransactionsEvent event, Acknowledgment acknowledgment) {
        List<Transaction> transactions = event.getTransactions();
        Long accountNumber = event.getAccountNumber();
        log.info("Received account-transactions-received event with {} transactions for account number: {}",
                transactions.size(), accountNumber);
        cache.addAccountTransactionsToCache(accountNumber, transactions);
        CountDownLatch latch = latchService.getLatch();
        if (latch != null) {
            latch.countDown();
        }
        acknowledgment.acknowledge();
    }
}