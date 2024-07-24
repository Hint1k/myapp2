package com.bank.webservice.publisher;

import com.bank.webservice.dto.Customer;
import com.bank.webservice.event.customer.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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
        kafkaTemplate.send("customer-creation-requested", event);
        log.info("Published customer-creation-requested event for customer number: {}", event.getCustomerNumber());
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
        kafkaTemplate.send("customer-update-requested", event);
        log.info("Published customer-update-requested event for customer id: {}", event.getCustomerId());
    }

    @Override
    public void publishCustomerDeletedEvent(Long customerId) {
        CustomerDeletedEvent event = new CustomerDeletedEvent(
                customerId,
                null
        );
        kafkaTemplate.send("customer-deletion-requested", event);
        log.info("Published customer-deletion-requested event for customer id: {}", event.getCustomerId());
    }

    @Override
    public void publishAllCustomersEvent() {
        AllCustomersEvent event = new AllCustomersEvent(new ArrayList<>());
        kafkaTemplate.send("all-customers-requested", event);
        log.info("Published all-customers-requested event");
    }

    @Override
    public void publishCustomerDetailsEvent(Long customerId) {
        CustomerDetailsEvent event = new CustomerDetailsEvent(
                customerId,
                null,
                null,
                null,
                null,
                null,
                null
        );
//        CustomerDetailsEvent event = new CustomerDetailsEvent(
//                customerId,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
        kafkaTemplate.send("customer-details-requested", event);
        log.info("Published customer-details-requested event for customer id: {}", event.getCustomerId());
    }
}