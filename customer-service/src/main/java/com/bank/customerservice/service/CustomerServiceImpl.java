package com.bank.customerservice.service;

import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.publisher.CustomerEventPublisher;
import com.bank.customerservice.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerEventPublisher publisher;

    @Autowired
    public CustomerServiceImpl(CustomerRepository repository, CustomerEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public void saveCustomer(Customer customer) {
        repository.save(customer);
        publisher.publishCustomerCreatedEvent(customer);
        log.info("Customer saved: {}", customer);
    }

    @Override
    @Transactional
    public void updateCustomer(Customer customer) {
        repository.save(customer);
        publisher.publishCustomerUpdatedEvent(customer);
        log.info("Customer with id: {} updated", customer.getCustomerId());
    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = repository.findById(customerId).orElse(null);
        if (customer == null) {
            handleNullCustomer(customerId);
        } else {
            Long customerNumber = customer.getCustomerNumber();
            repository.deleteById(customerId);
            publisher.publishCustomerDeletedEvent(customerId, customerNumber);
            log.info("Customer with id: {} has been deleted", customerId);
        }
    }

    @Override
    @Transactional
    public List<Customer> findAllCustomers() {
        List<Customer> customers = repository.findAll();
        publisher.publishAllCustomersEvent(customers);
        log.info("Retrieved {} customers", customers.size());
        return customers;
    }

    @Override
    @Transactional
    public Customer findCustomerById(Long customerId) {
        Customer customer = repository.findById(customerId).orElse(null);
        if (customer == null) {
            handleNullCustomer(customerId);
        }
        publisher.publishCustomerDetailsEvent(customer);
        return customer;
    }

    private void handleNullCustomer(Long customerId) {
        // TODO return message to the web-service
        log.error("Customer with id {} not found", customerId);
        throw new EntityNotFoundException("Customer with id " + customerId + " not found");
    }
}