package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Account;
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
public class AccountCacheImpl implements AccountCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "account:";
    private final CacheService service;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AccountCacheImpl(CacheService service, RedisTemplate<String, Object> redisTemplate) {
        this.service = service;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<Account> getAllAccountsFromCache() {
        Set<String> allKeys = service.getAllKeys(PREFIX);
        return service.getObjectsFromCache(allKeys, Account.class);
    }

    @Override
    public Account getAccountFromCache(Long accountId) {
        return (Account) redisTemplate.opsForValue().get(PREFIX + accountId.toString());
    }

    @Override
    public Account getAccountFromCacheByAccountNumber(Long accountNumber) {
        List<Account> accounts = getAllAccountsFromCache();
        return accounts.stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst().orElse(null);
    }

    @Override
    public List<Account> getAccountsFromCacheByCustomerNumber(Long customerNumber) {
        List<Account> allAccounts = getAllAccountsFromCache();
        return allAccounts.stream()
                .filter(a -> a.getCustomerNumber().equals(customerNumber))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getAccountNumbersFromCacheByCustomerNumber(Long customerNumber) {
        List<Account> accounts = getAccountsFromCacheByCustomerNumber(customerNumber);
        return accounts.stream()
                .map(Account::getAccountNumber)
                .distinct()
                .collect(Collectors.toList());
    }
}