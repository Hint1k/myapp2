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
            // TODO return message to the web-service
            log.error("Customer with id {} not found", customerId);
            throw new EntityNotFoundException("Customer with id " + customerId + " not found");
        }
        Long customerNumber = customer.getCustomerNumber();
        repository.deleteById(customerId);
        publisher.publishCustomerDeletedEvent(customerId, customerNumber);
        log.info("Customer with id: {} has been deleted", customerId);
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
            // TODO return message to the web-service
            log.error("Customer with id {} not found", customerId);
            throw new EntityNotFoundException("Customer with id " + customerId + " not found");
        }
        publisher.publishCustomerDetailsEvent(customer);
        return customer;
    }

    @Override
    @Transactional
    public Customer findCustomerByItsNumber(Long customerNumber) {
        Customer customer = repository.findCustomerByCustomerNumber(customerNumber);
        if (customer == null) {
            // TODO return message to the web-service
            log.error("Customer with number: {} not found", customerNumber);
            throw new EntityNotFoundException("Customer with number " + customerNumber + " not found");
        }
        log.info("Retrieved customer with number: {}", customerNumber);
        return customer;
    }

    @Override
    @Transactional
    public Customer findCustomerByAccountNumber(String accountNumber) {
        Customer customer = repository.findCustomerByAccountNumber(accountNumber);
        return customer;
    }

    @Override
    @Transactional
    public void updateCustomerAccount(Long customerNumber, String accountNumber) {
        // Removing the account number from another customer if there is any
        Customer customer1 = findCustomerByAccountNumber(accountNumber);
        if (customer1 != null) {
            String accountNumbers1 = customer1.getAccountNumbers();
            if (accountNumbers1.equals(accountNumber)) {
                accountNumbers1 = "";
            } else {
                accountNumbers1 = accountNumbers1.replace(accountNumber + ",", "");
                accountNumbers1 = accountNumbers1.replace("," + accountNumber, "");
            }
            customer1.setAccountNumbers(accountNumbers1);
            updateCustomer(customer1);
        }
        // Assigning the account number to the target customer
        Customer customer2 = findCustomerByItsNumber(customerNumber);
        String accountNumbers2 = "";
        if (customer2.getAccountNumbers() == null || customer2.getAccountNumbers().isEmpty()) {
            accountNumbers2 = accountNumber; // to avoid having "null," or "," as one of account numbers
        } else {
            accountNumbers2 = customer2.getAccountNumbers() + "," + accountNumber;
        }
        customer2.setAccountNumbers(accountNumbers2);
        updateCustomer(customer2);
    }
}