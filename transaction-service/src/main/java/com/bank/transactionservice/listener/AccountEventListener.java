package com.bank.transactionservice.listener;

import com.bank.transactionservice.entity.Account;
import com.bank.transactionservice.event.AccountCreatedEvent;
import com.bank.transactionservice.service.TransactionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventListener {

    @Autowired
    private TransactionServiceImpl transactionServiceImpl;

    @KafkaListener(topics = "account-created", groupId = "transaction-service")
    public void handleAccountCreatedEvent(AccountCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received account-created event for account number: {}", event.getAccountNumber());
        try {
            Account account = transactionServiceImpl.createInitialTransaction(event);
            log.info("Created initial transaction for account number: {}", account.getAccountNumber());
            acknowledgment.acknowledge();
        } catch (Exception exception) {
            log.error("Failed to handle account creation event", exception);
            // TODO Handle exception here later
        }
    }
}