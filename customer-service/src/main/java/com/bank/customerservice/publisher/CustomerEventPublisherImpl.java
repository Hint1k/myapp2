package com.bank.customerservice.publisher;

import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CustomerEventPublisherImpl implements CustomerEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public CustomerEventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishCustomerCreatedEvent(Customer customer) {
        CustomerCreatedEvent event = new CustomerCreatedEvent(
                customer.getCustomerId(),
                customer.getCustomerNumber(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getMiddleName(),
                customer.getEmail(),
                customer.getPhone()
//                , customer.getAddress()
//              ,  customer.getAccountNumbers()
        );
        kafkaTemplate.send("customer-created", event);
        log.info("Published customer-created event for customer id: {}", event.getCustomerId());
        //TODO add check later with completableFuture
    }

    @Override
    public void publishCustomerUpdatedEvent(Customer customer) {
        CustomerUpdatedEvent event = new CustomerUpdatedEvent(
                // TODO remove fields that cannot be updated later
                customer.getCustomerId(),
                customer.getCustomerNumber(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getMiddleName(),
                customer.getEmail(),
                customer.getPhone()
//                , customer.getAddress()
//              ,  customer.getAccountNumbers()
        );
        kafkaTemplate.send("customer-updated", event);
        log.info("Published customer-updated event for customer id: {}", event.getCustomerId());
        // TODO add check later with completableFuture
    }

    @Override
    public void publishCustomerDeletedEvent(Long customerId, Long customerNumber) {
        CustomerDeletedEvent event = new CustomerDeletedEvent(
                customerId,
                customerNumber
        );
        kafkaTemplate.send("customer-deleted", event);
        log.info("Published customer-deleted event for customer id: {}", event.getCustomerId());
        // TODO add check later with completableFuture
    }

    @Override
    public void publishAllCustomersEvent(List<Customer> customers) {
        AllCustomersEvent event = new AllCustomersEvent(customers);
        kafkaTemplate.send("all-customers-received", event);
        log.info("Published all-customers-received event with {} customers", customers.size());
        // TODO add check later with completableFuture
    }

    @Override
    public void publishCustomerDetailsEvent(Customer customer) {
        CustomerDetailsEvent event = new CustomerDetailsEvent(
                customer.getCustomerId(),
                customer.getCustomerNumber(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getMiddleName(),
                customer.getEmail(),
                customer.getPhone()
//                , customer.getAddress()
//              ,  customer.getAccountNumbers()
        );
        kafkaTemplate.send("customer-details-received", event);
        log.info("Published customer-details-received event for customer id: {}", event.getCustomerId());
        // TODO add check later with completableFuture
    }
}