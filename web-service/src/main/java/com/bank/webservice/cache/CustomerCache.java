package com.bank.webservice.cache;

import com.bank.webservice.dto.Customer;

import java.util.List;

public interface CustomerCache {

    void addCustomerToCache(Long customerId, Customer customer);

    void addAllCustomersToCache(List<Customer> customers);

    void updateCustomerInCache(Long customerId, Customer customer);

    void deleteCustomerFromCache(Long customerId);

    List<Customer> getAllCustomersFromCache();

    Customer getCustomerFromCache(Long customerId);

    Customer getCustomerFromCacheByCustomerNumber(Long customerNumber);
}