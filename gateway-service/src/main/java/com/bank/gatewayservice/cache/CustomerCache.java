package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Customer;

import java.util.List;

public interface CustomerCache {

    List<Customer> getAllCustomersFromCache();

    Customer getCustomerFromCache(Long customerId);

    Customer getCustomerFromCacheByCustomerNumber(Long customerNumber);
}