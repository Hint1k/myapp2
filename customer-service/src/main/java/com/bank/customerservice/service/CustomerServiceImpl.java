package com.bank.customerservice.service;

import com.bank.customerservice.entity.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Override
    public void saveCustomer(Customer customer) {

    }

    @Override
    public void updateCustomer(Customer customer) {

    }

    @Override
    public void deleteCustomer(Long customerId) {

    }

    @Override
    public List<Customer> findAllCustomers() {
        return List.of();
    }

    @Override
    public Customer findCustomerById(Long customerId) {
        return null;
    }

    @Override
    public Customer findCustomerByItsNumber(Long customerNumber) {
        return null;
    }
}