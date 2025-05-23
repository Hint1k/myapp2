package com.bank.webservice.cache;

import com.bank.webservice.dto.Account;
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
public class AccountCacheImpl implements AccountCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "account:";
    private final CacheService service;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addAccountToCache(Long accountId, Account account) {
        redisTemplate.opsForValue().set(PREFIX + accountId.toString(), account);
    }

    @Override
    public void addAllAccountsToCache(List<Account> accounts) {
        for (Account account : accounts) {
            redisTemplate.opsForValue().set(PREFIX + account.getAccountId().toString(), account);
        }
    }

    @Override
    public void updateAccountInCache(Long accountId, Account account) {
        String key = PREFIX + accountId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, account);
        } else {
            log.error("Account with id {} not found", accountId);
        }
    }

    @Override
    public void deleteAccountFromCache(Long accountId) {
        redisTemplate.delete(PREFIX + accountId.toString());
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