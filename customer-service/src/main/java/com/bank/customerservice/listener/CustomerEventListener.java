package com.bank.customerservice.listener;

import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.event.customer.CustomerCreatedEvent;
import com.bank.customerservice.event.customer.CustomerDeletedEvent;
import com.bank.customerservice.event.customer.CustomerDetailsEvent;
import com.bank.customerservice.event.customer.CustomerUpdatedEvent;
import com.bank.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerEventListener {

    private final CustomerService service;

    @KafkaListener(topics = "customer-creation-requested", groupId = "customer-service")
    public void handleCustomerCreatedEvent(CustomerCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received customer-creation-requested event for customer number: {}", event.getCustomerNumber());
        Customer customer = new Customer(
                event.getCustomerId(),
                event.getCustomerNumber(),
                event.getName(),
                event.getEmail(),
                event.getPhone(),
                event.getAddress(),
                event.getAccountNumbers()
        );
        try {
            service.saveCustomer(customer);
            log.info("Saved customer number: {}", customer.getCustomerNumber());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error saving customer: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "customer-update-requested", groupId = "customer-service")
    public void handleCustomerUpdatedEvent(CustomerUpdatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received customer-update-requested event for customer id: {}", event.getCustomerId());
        Customer customer = new Customer(
                event.getCustomerId(),
                event.getCustomerNumber(),
                event.getName(),
                event.getEmail(),
                event.getPhone(),
                event.getAddress(),
                event.getAccountNumbers()
        );
        try {
            service.updateCustomer(customer);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error updating customer by id: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "customer-deletion-requested", groupId = "customer-service")
    public void handleCustomerDeletedEvent(CustomerDeletedEvent event, Acknowledgment acknowledgment) {
        Long customerId = event.getCustomerId();
        log.info("Received customer-deletion-requested event for customer id: {}", customerId);
        try {
            service.deleteCustomer(customerId);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error deleting customer by id: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "all-customers-requested", groupId = "customer-service")
    public void handleAllCustomersEvent(Acknowledgment acknowledgment) {
        log.info("Received all-customers-requested event");
        try {
            service.findAllCustomers();
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error finding all customers: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "customer-details-requested", groupId = "customer-service")
    public void handleCustomerDetailsEvent(CustomerDetailsEvent event, Acknowledgment acknowledgment) {
        Long customerId = event.getCustomerId();
        log.info("Received customer-details-requested event for customer id: {}", customerId);
        try {
            service.findCustomerById(customerId);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error finding customer by id: {}", e.getMessage());
        }
    }
}