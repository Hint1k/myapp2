package com.bank.webservice.cache;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TransactionCacheImpl implements TransactionCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "transaction:";
    private final CacheService service;

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public TransactionCacheImpl(CacheService service, RedisTemplate<String, Object> redisTemplate) {
        this.service = service;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addTransactionToCache(Long transactionId, Transaction transaction) {
        redisTemplate.opsForValue().set(PREFIX + transactionId.toString(), transaction);
    }

    @Override
    public void addAllTransactionsToCache(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            redisTemplate.opsForValue().set(PREFIX + transaction.getTransactionId().toString(), transaction);
        }
    }

    @Override
    public void updateTransactionFromCache(Long transactionId, Transaction transaction) {
        String key = PREFIX + transactionId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, transaction);
        } else {
            log.error("Transaction id {} not found", transactionId);
            // TODO handle cases when transaction is not found.
        }
    }

    @Override
    public void deleteTransactionFromCache(Long transactionId) {
        redisTemplate.delete(PREFIX + transactionId.toString());
    }

    @Override
    public List<Transaction> getAllTransactionsFromCache() {
        Set<String> allKeys = service.getAllKeys(PREFIX);
        return service.getObjectsFromCache(allKeys, Transaction.class);
    }

    @Override
    public Transaction getTransactionFromCache(Long transactionId) {
        return (Transaction) redisTemplate.opsForValue().get(PREFIX + transactionId.toString());
    }

    @Override
    public List<Transaction> getAccountTransactionsFromCache(Long accountNumber) {
        List<Transaction> allTransactions = getAllTransactionsFromCache();
        return allTransactions.stream()
                .filter(t -> t.getAccountSourceNumber().equals(accountNumber) ||
                        t.getAccountDestinationNumber().equals(accountNumber))
                .distinct()
                .collect(Collectors.toList());
    }
}