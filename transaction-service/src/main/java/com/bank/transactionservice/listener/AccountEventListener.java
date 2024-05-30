package com.bank.transactionservice.listener;

import com.bank.transactionservice.dto.AccountCreatedEventDTO;
import com.bank.transactionservice.dto.AccountDTO;
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

    @KafkaListener(topics = "account-saved", groupId = "transaction-service")
    public void handleAccountCreatedEvent(AccountCreatedEventDTO event) {
        log.info("Received AccountCreatedEvent: {}", event);
        try {
            AccountDTO account = transactionServiceImpl.createInitialTransaction(event);
            log.info("Created initial transaction for account: {}", account.getAccountNumber());
        } catch (Exception exception) {
            log.error("Failed to handle account creation event", exception);
            // Handle exception here later
        }
    }
}