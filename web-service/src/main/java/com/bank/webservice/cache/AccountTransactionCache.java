package com.bank.webservice.cache;

import com.bank.webservice.dto.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AccountTransactionCache {

    // account and transaction cache with the same id causes error
    private static final String PREFIX = "account_transactions:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AccountTransactionCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addAccountTransactionsToCache(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            redisTemplate.opsForValue().set(PREFIX + transaction.getTransactionId().toString(), transaction);
        }
    }

    public List<Transaction> getTransactionsFromCacheByAccountId(Long accountId) {
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
}