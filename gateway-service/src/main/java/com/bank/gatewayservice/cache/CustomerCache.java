package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Customer;

public interface CustomerCache {

    Customer getCustomerFromCache(Long customerId);
}