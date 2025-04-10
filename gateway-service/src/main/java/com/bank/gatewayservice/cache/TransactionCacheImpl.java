package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionCacheImpl implements TransactionCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "transaction:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Transaction getTransactionFromCache(Long transactionId) {
        return (Transaction) redisTemplate.opsForValue().get(PREFIX + transactionId.toString());
    }
}