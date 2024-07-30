package com.bank.customerservice.service;

import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.publisher.CustomerEventPublisher;
import com.bank.customerservice.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final CustomerRepository repository;
    private final CustomerEventPublisher publisher;

    @Autowired
    public AccountServiceImpl(CustomerRepository repository, CustomerEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public void updateCustomerDueToAccountChange(Long customerNumber, String accountNumber) {
        removeAccountNumberFromCustomer(accountNumber);
        if (customerNumber != null) {
            assignAccountNumberToCustomer(customerNumber, accountNumber);
        }
    }

    private void removeAccountNumberFromCustomer(String accountNumber) {
        Customer customer = repository.findCustomerByAccountNumber(accountNumber);
        if (customer != null) {
            String accountNumbers = customer.getAccountNumbers();
            if (accountNumbers.equals(accountNumber)) {
                accountNumbers = "";
            } else {
                accountNumbers = accountNumbers.replace(accountNumber + ",", "");
                accountNumbers = accountNumbers.replace("," + accountNumber, "");
            }
            customer.setAccountNumbers(accountNumbers);
            updateCustomer(customer);
        }
    }

    private void assignAccountNumberToCustomer(Long customerNumber, String accountNumber) {
        Customer customer = repository.findCustomerByCustomerNumber(customerNumber);
        if (customer != null) {
            String accountNumbers = "";
            if (customer.getAccountNumbers() == null || customer.getAccountNumbers().isEmpty()) {
                accountNumbers = accountNumber; // to avoid having "null," or "," as one of account numbers
            } else {
                accountNumbers = customer.getAccountNumbers() + "," + accountNumber;
            }
            customer.setAccountNumbers(accountNumbers);
            updateCustomer(customer);
        } else {
            // TODO return message to the web-service
            log.error("Customer with customer number {} not found", customerNumber);
            throw new EntityNotFoundException("Customer with customer number " + customerNumber + " not found");
        }
    }

    private void updateCustomer(Customer customer) {
        repository.save(customer);
        publisher.publishCustomerDetailsEvent(customer);
        log.info("Customer with id: {} updated due to account change", customer.getCustomerId());
    }
}