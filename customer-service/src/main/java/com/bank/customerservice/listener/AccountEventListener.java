package com.bank.customerservice.listener;

import com.bank.customerservice.event.account.AccountCreatedEvent;
import com.bank.customerservice.event.account.AccountDeletedEvent;
import com.bank.customerservice.event.account.AccountUpdatedEvent;
import com.bank.customerservice.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventListener {

    private final AccountService service;

    public AccountEventListener(AccountService service) {
        this.service = service;
    }

    @KafkaListener(topics = "account-created", groupId = "customer-service")
    public void handleAccountCreatedEvent(AccountCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received account-created event for account number: {}", event.getAccountNumber());
        service.updateCustomerDueToAccountChange(event.getCustomerNumber(), String.valueOf(event.getAccountNumber()));
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "account-updated", groupId = "customer-service")
    public void handleAccountUpdatedEvent(AccountUpdatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received account-updated event for account number: {}", event.getAccountNumber());
        service.updateCustomerDueToAccountChange(event.getCustomerNumber(), String.valueOf(event.getAccountNumber()));
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "account-deleted", groupId = "customer-service")
    public void handleAccountDeletedEvent(AccountDeletedEvent event, Acknowledgment acknowledgment) {
        Long accountNumber = event.getAccountNumber();
        log.info("Received account-deleted event for account number: {}", accountNumber);
        service.updateCustomerDueToAccountChange(null, String.valueOf(event.getAccountNumber()));
        acknowledgment.acknowledge();
    }
}