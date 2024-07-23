package com.bank.webservice.publisher;

import com.bank.webservice.dto.Customer;

public interface CustomerEventPublisher {

    void publishCustomerCreatedEvent(Customer customer);

    void publishCustomerUpdatedEvent(Customer customer);

    void publishCustomerDeletedEvent(Long customerId);

    void publishAllCustomersEvent();

    void publishCustomerDetailsEvent(Long customerId);
}