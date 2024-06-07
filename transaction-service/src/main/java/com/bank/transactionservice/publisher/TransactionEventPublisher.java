package com.bank.transactionservice.publisher;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.event.transaction.TransactionCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionEventPublisher {

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    @Autowired
    public TransactionEventPublisher(KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransactionCreatedEvent(Transaction transaction) {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getAccount()
        );
        kafkaTemplate.send("transaction-creation-completed", event);
        log.info("Published transaction completed event for account number: {}",
                event.getAccount().getAccountNumber());
    }
}