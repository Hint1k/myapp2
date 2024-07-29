package com.bank.customerservice.listener;

import com.bank.customerservice.event.account.AccountCreatedEvent;
import com.bank.customerservice.event.account.AccountUpdatedEvent;
import com.bank.customerservice.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventListener {

    private final CustomerService service;

    public AccountEventListener(CustomerService service) {
        this.service = service;
    }

    @KafkaListener(topics = "account-created", groupId = "customer-service")
    public void handleAccountCreatedEvent(AccountCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received account-created event for account number: {}", event.getAccountNumber());
        service.updateCustomerAccount(event.getCustomerNumber(), String.valueOf(event.getAccountNumber()));
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "account-updated", groupId = "customer-service")
    public void handleAccountUpdatedEvent(AccountUpdatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received account-updated event for account number: {}", event.getAccountNumber());
        service.updateCustomerAccount(event.getCustomerNumber(), String.valueOf(event.getAccountNumber()));
        acknowledgment.acknowledge();
    }
}