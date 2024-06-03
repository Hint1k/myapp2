package com.bank.transactionservice.publisher;

import com.bank.transactionservice.entity.Account;
import com.bank.transactionservice.event.InitialTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionEventPublisher {

    @Autowired
    private KafkaTemplate<String, InitialTransactionEvent> kafkaTemplate;

    public void publishInitialTransactionEvent(Account account) {
        InitialTransactionEvent event = new InitialTransactionEvent(
                account.getAccountNumber(),
                account.getBalance()
        );
        kafkaTemplate.send("initial-transaction-made", event);
        log.info("Published initial transaction event for account number: {}", event.getAccountNumber());
    }
}