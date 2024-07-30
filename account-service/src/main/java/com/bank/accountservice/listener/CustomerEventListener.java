package com.bank.accountservice.listener;

import com.bank.accountservice.event.customer.CustomerDeletedEvent;
import com.bank.accountservice.event.customer.CustomerUpdatedEvent;
import com.bank.accountservice.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerEventListener {

    private final CustomerService service;

    @Autowired
    public CustomerEventListener(CustomerService service) {
        this.service = service;
    }

    @KafkaListener(topics = "customer-updated", groupId = "account-service")
    public void handleCustomerUpdatedEvent(CustomerUpdatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received customer-updated event for customer id: {}", event.getCustomerId());
        Long customerNumber = event.getCustomerNumber();
        String accountNumbers = event.getAccountNumbers();
        service.updateAccountDueToCustomerChange(customerNumber, accountNumbers);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "customer-deleted", groupId = "account-service")
    public void handleCustomerDeletedEvent(CustomerDeletedEvent event, Acknowledgment acknowledgment) {
        log.info("Received customer-deleted event for customer id: {}", event.getCustomerId());
        Long customerNumber = event.getCustomerNumber();
        service.updateAccountDueToCustomerChange(customerNumber, null);
        acknowledgment.acknowledge();
    }
}