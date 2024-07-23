package com.bank.customerservice.publisher;

import com.bank.customerservice.entity.Customer;

import java.util.List;

public interface CustomerEventPublisher {

    void publishCustomerCreatedEvent(Customer customer);

    void publishCustomerUpdatedEvent(Customer customer);

    void publishCustomerDeletedEvent(Long customerId, Long customerNumber);

    void publishAllCustomersEvent(List<Customer> customers);

    void publishCustomerDetailsEvent(Customer customer);
}