package com.bank.webservice.cache;

import com.bank.webservice.dto.Customer;
import com.bank.webservice.service.CacheService;
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
    public void addCustomerToCache(Long customerId, Customer customer) {
        redisTemplate.opsForValue().set(PREFIX + customerId.toString(), customer);
    }

    @Override
    public void addAllCustomersToCache(List<Customer> customers) {
        for (Customer customer : customers) {
            redisTemplate.opsForValue().set(PREFIX + customer.getCustomerId().toString(), customer);
        }
    }

    @Override
    public void updateCustomerInCache(Long customerId, Customer customer) {
        String key = PREFIX + customerId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, customer);
        } else {
            log.error("customer with id {} not found", customerId);
            // TODO handle cases when customer is not found.
        }
    }

    @Override
    public void deleteCustomerFromCache(Long customerId) {
        redisTemplate.delete(PREFIX + customerId.toString());
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