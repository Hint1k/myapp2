package com.bank.webservice.cache;

import com.bank.webservice.dto.Account;
import com.bank.webservice.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class AccountCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "account:";
    private final CacheService cacheService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AccountCache(CacheService cacheService, RedisTemplate<String, Object> redisTemplate) {
        this.cacheService = cacheService;
        this.redisTemplate = redisTemplate;
    }

    public void addAccountToCache(Long accountId, Account account) {
        redisTemplate.opsForValue().set(PREFIX + accountId.toString(), account);
    }

    public void addAllAccountsToCache(List<Account> accounts) {
        for (Account account : accounts) {
            redisTemplate.opsForValue().set(PREFIX + account.getAccountId().toString(), account);
        }
    }

    public void updateAccountInCache(Long accountId, Account account) {
        String key = PREFIX + accountId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, account);
        } else {
            log.error("Account with id {} not found", accountId);
            // TODO handle cases when account is not found.
        }
    }

    public void deleteAccountFromCache(Long accountId) {
        redisTemplate.delete(PREFIX + accountId.toString());
    }

    public List<Account> getAllAccountsFromCache() {
        //Retrieve all keys from Redis
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        return cacheService.getObjectsFromCache(keys, Account.class);
    }

    public Account getAccountFromCache(Long accountId) {
        return (Account) redisTemplate.opsForValue().get(PREFIX + accountId.toString());
    }

    public Account getAccountFromCacheByAccountNumber(Long accountNumber) {
        List<Account> accounts = getAllAccountsFromCache();
        return accounts.stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber)).findFirst().orElse(null);
    }
}