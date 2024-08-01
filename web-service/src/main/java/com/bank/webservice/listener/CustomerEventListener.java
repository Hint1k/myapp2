package com.bank.webservice.listener;

import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.event.customer.AllCustomersEvent;
import com.bank.webservice.event.customer.CustomerCreatedEvent;
import com.bank.webservice.event.customer.CustomerDeletedEvent;
import com.bank.webservice.event.customer.CustomerUpdatedEvent;
import com.bank.webservice.event.customer.CustomerDetailsEvent;
import com.bank.webservice.service.LatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class CustomerEventListener {

    private final LatchService latch;
    private final CustomerCache cache;

    @Autowired
    public CustomerEventListener(LatchService latch, CustomerCache cache) {
        this.latch = latch;
        this.cache = cache;
    }

    @KafkaListener(topics = "customer-created", groupId = "web-service")
    public void handleCustomerCreatedEvent(CustomerCreatedEvent event, Acknowledgment acknowledgment) {
        Customer customer = new Customer(
                event.getCustomerId(),
                event.getCustomerNumber(),
                event.getName(),
                event.getEmail(),
                event.getPhone(),
                event.getAddress(),
                event.getAccountNumbers()
        );
        log.info("Received customer-created event for customer id: {}", event.getCustomerId());
        cache.addCustomerToCache(customer.getCustomerId(), customer);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "customer-updated", groupId = "web-service")
    public void handleCustomerUpdatedEvent(CustomerUpdatedEvent event, Acknowledgment acknowledgment) {
        Customer customer = new Customer(
                // TODO remove fields that cannot be updated later
                event.getCustomerId(),
                event.getCustomerNumber(),
                event.getName(),
                event.getEmail(),
                event.getPhone(),
                event.getAddress(),
                event.getAccountNumbers()
        );
        Long customerId = event.getCustomerId();
        log.info("Received customer-updated event for customer id: {}", customerId);
        cache.updateCustomerInCache(customerId, customer);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "customer-deleted", groupId = "web-service")
    public void handleCustomerDeletedEvent(CustomerDeletedEvent event, Acknowledgment acknowledgment) {
        Long customerId = event.getCustomerId();
        log.info("Received customer-deleted event for customer id: {}", customerId);
        cache.deleteCustomerFromCache(customerId);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "all-customers-received", groupId = "web-service")
    public void handleAllCustomersEvent(AllCustomersEvent event, Acknowledgment acknowledgment) {
        List<Customer> customers = event.getCustomers();
        log.info("Received all-customers-received event with {} customers", customers.size());
        cache.addAllCustomersToCache(customers);
        CountDownLatch latch = this.latch.getLatch(); // latch initialisation is in CustomerController class
        if (latch != null) {
            latch.countDown();
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "customer-details-received", groupId = "web-service")
    public void handleCustomerDetailsEvent(CustomerDetailsEvent event, Acknowledgment acknowledgment) {
        Customer customer = new Customer(
                event.getCustomerId(),
                event.getCustomerNumber(),
                event.getName(),
                event.getEmail(),
                event.getPhone(),
                event.getAddress(),
                event.getAccountNumbers()
        );
        log.info("Received customer-details-received event for customer id: {}", event.getCustomerId());
        cache.addCustomerToCache(customer.getCustomerId(), customer);
        acknowledgment.acknowledge();
    }
}