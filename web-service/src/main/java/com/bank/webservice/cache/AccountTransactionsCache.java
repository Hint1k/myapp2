package com.bank.webservice.cache;

import com.bank.webservice.dto.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AccountTransactionsCache {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AccountTransactionsCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addAccountTransactionsToCache(Long accountNumber, List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            redisTemplate.opsForValue().set(accountNumber + transaction.getTransactionId().toString(), transaction);
        }
    }

    public List<Transaction> getAccountTransactionsFromCache(Long accountNumber) {
        // Retrieve all keys from Redis
        Set<String> keys = redisTemplate.keys(accountNumber + "*");

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
}