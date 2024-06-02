package com.bank.transactionservice.listener;

import com.bank.transactionservice.dto.AccountCreatedEvent;
import com.bank.transactionservice.dto.Account;
import com.bank.transactionservice.service.TransactionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j // logger
public class AccountEventListener {

    @Autowired
    private TransactionServiceImpl transactionServiceImpl;

    @KafkaListener(topics = "account-created", groupId = "transaction-service")
    public void handleAccountCreatedEvent(AccountCreatedEvent event) {
        log.info("Received account-created event: {}", event);
        try {
            Account account = transactionServiceImpl.createInitialTransaction(event);
            log.info("Created initial transaction for account: {}", account.getAccountNumber());
        } catch (Exception exception) {
            log.error("Failed to handle account creation event", exception);
            // TODO Handle exception here later
        }
    }
}