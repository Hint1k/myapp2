package com.bank.webservice.publisher;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.transaction.*;
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
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-creation-requested", event);
        log.info("Published transaction-creation-requested event for account source number: {}",
                event.getAccountDestinationNumber());
    }

    public void publishTransactionUpdatedEvent(Transaction transaction) {
        TransactionUpdatedEvent event = new TransactionUpdatedEvent(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-update-requested", event);
        log.info("Published transaction-update-requested event for transaction id: {}", event.getTransactionId());
    }

    public void publishTransactionDeletedEvent(Long transactionId) {
        TransactionDeletedEvent event = new TransactionDeletedEvent(transactionId);
        kafkaTemplate.send("transaction-deletion-requested", event);
        log.info("Published transaction-deletion-requested event for transaction id: {}", event.getTransactionId());
    }

    public void publishAllTransactionsEvent(List<Transaction> transactions) {
        AllTransactionsEvent event = new AllTransactionsEvent(transactions);
        kafkaTemplate.send("all-transactions-requested", event);
        log.info("Published all-transactions-requested event for {} transactions", transactions.size());
    }

    public void publishTransactionDetailsEvent(Transaction transaction) {
        TransactionDetailsEvent event = new TransactionDetailsEvent(
                transaction.getTransactionId(),
                null,
                null,
                null,
                null
        );

        kafkaTemplate.send("transaction-details-requested", event);
        log.info("Published transaction-details-requested event for transaction id: {}", event.getTransactionId());
    }
}