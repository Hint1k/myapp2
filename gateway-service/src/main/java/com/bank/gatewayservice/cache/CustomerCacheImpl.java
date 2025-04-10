package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerCacheImpl implements CustomerCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "customer:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Customer getCustomerFromCache(Long customerId) {
        return (Customer) redisTemplate.opsForValue().get(PREFIX + customerId.toString());
    }
}