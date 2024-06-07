package com.bank.webservice.cache;

import com.bank.webservice.dto.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionCache {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public TransactionCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addTransactionToCache(Long transactionId, Transaction transaction) {
        redisTemplate.opsForValue().set(transactionId.toString(), transaction);
    }
}