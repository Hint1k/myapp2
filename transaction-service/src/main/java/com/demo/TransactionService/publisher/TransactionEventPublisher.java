package com.demo.TransactionService.publisher;

import com.demo.TransactionService.dto.AccountDTO;
import com.demo.TransactionService.event.TransactionCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionEventPublisher {

    @Autowired
    private KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    public void publishTransactionCreatedEvent(AccountDTO account) {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                account.getBalance(),
                account.getAccountNumber()
        );
        kafkaTemplate.send("transaction-created", event);
        log.info("Published transaction created event: {}", event);
    }
}