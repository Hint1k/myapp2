package com.bank.transactionservice.publisher;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public TransactionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransactionCreatedEvent(Transaction transaction) {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-created", event);
        log.info("Published transaction-created event for transaction id: {}", event.getTransactionId());
    }

    public void publishTransactionUpdatedEvent(Transaction transaction) {
        TransactionUpdatedEvent event = new TransactionUpdatedEvent(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-updated", event);
        log.info("Published transaction-updated event for transaction id: {}", event.getTransactionId());
    }

    public void publishTransactionDeletedEvent(Long transactionId) {
        TransactionDeletedEvent event = new TransactionDeletedEvent(transactionId);
        kafkaTemplate.send("transaction-deleted", event);
        log.info("Published transaction-deleted event for transaction id: {}", event.getTransactionId());
    }

    public void publishAllTransactionsEvent(List<Transaction> transactions) {
        AllTransactionsEvent event = new AllTransactionsEvent(transactions);
        kafkaTemplate.send("all-transactions-received", event);
        log.info("Published all-transactions-received event with {} transactions", transactions.size());
    }

    public void publishAccountTransactionsEvent(Long accountNumber, List<Transaction> transactions) {
        AccountTransactionsEvent event = new AccountTransactionsEvent(
                accountNumber,
                transactions
        );
        kafkaTemplate.send("account-transactions-received", event);
        log.info("Published account-transactions-received event with {} transactions for account number: {}",
                transactions.size(), accountNumber);
    }

    public void publishTransactionDetailsEvent(Transaction transaction) {
        TransactionDetailsEvent event = new TransactionDetailsEvent(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-details-received", event);
        log.info("Published transaction-details-received event for transaction id: {}", event.getTransactionId());
    }
}