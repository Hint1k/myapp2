package com.bank.webservice.cache;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionCacheImpl implements TransactionCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "transaction:";
    private final CacheService service;
    private final RedisTemplate<String, Object> redisTemplate;

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