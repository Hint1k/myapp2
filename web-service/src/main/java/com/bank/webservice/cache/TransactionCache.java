package com.bank.webservice.cache;

import com.bank.webservice.dto.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public void addAllTransactionsToCache(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            redisTemplate.opsForValue().set(transaction.getTransactionId().toString(), transaction);
        }
    }

    public Transaction getFromCacheById(Long transactionId) {
        return (Transaction) redisTemplate.opsForValue().get(transactionId.toString());
    }

    public List<Transaction> getAllTransactionsFromCache() {
        // Retrieve all keys from Redis
        Set<String> keys = redisTemplate.keys("*");

        List<Transaction> transactions = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                // Retrieve the value associated with the key from Redis
                Transaction transaction = (Transaction) redisTemplate.opsForValue().get(key);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    public void deleteTransactionFromCacheById(Long transactionId) {
        redisTemplate.delete(transactionId.toString());
    }

    public void updateTransactionFromCacheById(Long transactionId, Transaction transaction) {
        String key = transactionId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, transaction);
        } else {
            throw new IllegalArgumentException("Transaction with ID " + transactionId + " does not exist in the cache.");
        }
    }
}