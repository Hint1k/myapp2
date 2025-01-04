package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Customer;
import com.bank.gatewayservice.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class CustomerCacheImpl implements CustomerCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "customer:";
    private final CacheService service;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CustomerCacheImpl(CacheService service, RedisTemplate<String, Object> redisTemplate) {
        this.service = service;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<Customer> getAllCustomersFromCache() {
        Set<String> allKeys = service.getAllKeys(PREFIX);
        return service.getObjectsFromCache(allKeys, Customer.class);
    }

    @Override
    public Customer getCustomerFromCache(Long customerId) {
        return (Customer) redisTemplate.opsForValue().get(PREFIX + customerId.toString());
    }

    @Override
    public Customer getCustomerFromCacheByCustomerNumber(Long customerNumber) {
        List<Customer> customers = getAllCustomersFromCache();
        return customers.stream()
                .filter(customer -> customer.getCustomerNumber().equals(customerNumber))
                .findFirst().orElse(null);
    }
}