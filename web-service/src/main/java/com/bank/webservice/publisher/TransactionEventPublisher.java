package com.bank.webservice.publisher;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.transaction.TransactionCreatedEvent;
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

    public void publishTransactionCreatedEvent(Transaction transaction) {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getAccount()
        );
        kafkaTemplate.send("transaction-creation-requested", event);
        log.info("Published transaction-creation-requested event for account number: {}",
                event.getAccount().getAccountNumber());
        // add check later with CompletableFuture
    }
}