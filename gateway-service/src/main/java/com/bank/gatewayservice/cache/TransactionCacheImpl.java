package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Transaction;
import com.bank.gatewayservice.service.CacheService;
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
    public List<Transaction> getAllTransactionsFromCache() {
        Set<String> allKeys = service.getAllKeys(PREFIX);
        return service.getObjectsFromCache(allKeys, Transaction.class);
    }

    @Override
    public Transaction getTransactionFromCache(Long transactionId) {
        return (Transaction) redisTemplate.opsForValue().get(PREFIX + transactionId.toString());
    }

    @Override
    public List<Transaction> getTransactionsForAccountFromCache(Long accountNumber) {
        return filterTransactionsByAccountNumbers(List.of(accountNumber));
    }

    @Override
    public List<Transaction> getTransactionsForMultipleAccountsFromCache(List<Long> accountNumbers) {
        return filterTransactionsByAccountNumbers(accountNumbers);
    }

    private List<Transaction> filterTransactionsByAccountNumbers(List<Long> accountNumbers) {
        return getAllTransactionsFromCache().stream()
                .filter(transaction -> accountNumbers.stream().anyMatch(accountNumber ->
                        transaction.getAccountSourceNumber().equals(accountNumber) ||
                                transaction.getAccountDestinationNumber().equals(accountNumber)))
                .distinct()
                .collect(Collectors.toList());
    }
}