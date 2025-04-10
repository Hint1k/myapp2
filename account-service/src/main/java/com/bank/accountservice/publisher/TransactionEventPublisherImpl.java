package com.bank.accountservice.publisher;

import com.bank.accountservice.event.transaction.TransactionApprovedEvent;
import com.bank.accountservice.event.transaction.TransactionFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventPublisherImpl implements TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishTransactionFailedEvent(Long transactionId) {
        TransactionFailedEvent event = new TransactionFailedEvent(transactionId);
        kafkaTemplate.send("transaction-failed", event);
        log.info("Published transaction-failed event for transaction id: {}", transactionId);
    }

    @Override
    public void publishTransactionApprovedEvent(Long transactionId) {
        TransactionApprovedEvent event = new TransactionApprovedEvent(transactionId);
        kafkaTemplate.send("transaction-approved", event);
        log.info("Published transaction-approved event for transaction id: {}", transactionId);
    }
}