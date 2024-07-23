package com.bank.webservice.publisher;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.transaction.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Slf4j
public class TransactionEventPublisherImpl implements TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public TransactionEventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishTransactionCreatedEvent(Transaction transaction) {
        if (transaction.getAccountDestinationNumber() == null) {
            transaction.setAccountDestinationNumber(transaction.getAccountSourceNumber());
        }
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getAccountSourceNumber(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-creation-requested", event);
        log.info("Published transaction-creation-requested event for account source number: {}",
                event.getAccountDestinationNumber());
    }

    @Override
    public void publishTransactionUpdatedEvent(Transaction transaction) {
        if (transaction.getAccountDestinationNumber() == null) {
            transaction.setAccountDestinationNumber(transaction.getAccountSourceNumber());
        }
        TransactionUpdatedEvent event = new TransactionUpdatedEvent(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getAccountSourceNumber(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-update-requested", event);
        log.info("Published transaction-update-requested event for transaction id: {}", event.getTransactionId());
    }

    @Override
    public void publishTransactionDeletedEvent(Long transactionId) {
        TransactionDeletedEvent event = new TransactionDeletedEvent(transactionId);
        kafkaTemplate.send("transaction-deletion-requested", event);
        log.info("Published transaction-deletion-requested event for transaction id: {}", event.getTransactionId());
    }

    @Override
    public void publishAllTransactionsEvent() {
        AllTransactionsEvent event = new AllTransactionsEvent(new ArrayList<>());
        kafkaTemplate.send("all-transactions-requested", event);
        log.info("Published all-transactions-requested event");
    }

    @Override
    public void publishTransactionDetailsEvent(Transaction transaction) {
        TransactionDetailsEvent event = new TransactionDetailsEvent(
                transaction.getTransactionId(),
                null,
                null,
                null,
                null,
                null,
                null
        );
        kafkaTemplate.send("transaction-details-requested", event);
        log.info("Published transaction-details-requested event for transaction id: {}", event.getTransactionId());
    }
}