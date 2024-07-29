package com.bank.customerservice.service;

import com.bank.customerservice.entity.Customer;

import java.util.List;

public interface CustomerService {

    void saveCustomer(Customer customer);

    void updateCustomer(Customer customer);

    void deleteCustomer(Long customerId);

    List<Customer> findAllCustomers();

    Customer findCustomerById(Long customerId);

    Customer findCustomerByItsNumber(Long customerNumber);

    Customer findCustomerByAccountNumber(String accountNumber);

    void updateCustomerAccount(Long customerNumber, String accountNumber);
}