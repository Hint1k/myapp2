package com.bank.accountservice.publisher;

import com.bank.accountservice.event.TransactionApprovedEvent;
import com.bank.accountservice.event.TransactionFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public TransactionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransactionFailedEvent(Long transactionId) {
        TransactionFailedEvent event = new TransactionFailedEvent(transactionId);
        kafkaTemplate.send("transaction-failed", event);
        log.info("Published transaction-failed event for transaction id: {}", transactionId);
    }

    public void publishTransactionApprovedEvent(Long transactionId) {
        TransactionApprovedEvent event = new TransactionApprovedEvent(transactionId);
        kafkaTemplate.send("transaction-approved", event);
        log.info("Published transaction-approved event for transaction id: {}", transactionId);
    }
}