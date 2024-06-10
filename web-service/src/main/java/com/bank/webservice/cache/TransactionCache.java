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

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "transaction:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public TransactionCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addTransactionToCache(Long transactionId, Transaction transaction) {
        redisTemplate.opsForValue().set(PREFIX + transactionId.toString(), transaction);
    }

    public void addAllTransactionsToCache(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            redisTemplate.opsForValue().set(PREFIX + transaction.getTransactionId().toString(), transaction);
        }
    }

    public void updateTransactionFromCache(Long transactionId, Transaction transaction) {
        String key = PREFIX + transactionId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, transaction);
        } else {
            throw new IllegalArgumentException("Transaction with ID " + transactionId
                    + " does not exist in the cache.");
        }
    }

    public void deleteTransactionFromCache(Long transactionId) {
        redisTemplate.delete(PREFIX + transactionId.toString());
    }

    public List<Transaction> getAllTransactionsFromCache() {
        // Retrieve all keys from Redis
        Set<String> keys = redisTemplate.keys(PREFIX + "*");

        List<Transaction> transactions = null;
        if (keys != null) {
            transactions = new ArrayList<>();
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

    public Transaction getTransactionFromCache(Long transactionId) {
        return (Transaction) redisTemplate.opsForValue().get(PREFIX + transactionId.toString());
    }
}