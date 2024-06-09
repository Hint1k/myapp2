package com.bank.webservice.cache;

import com.bank.webservice.dto.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AccountCache {

    // account and transaction cache with the same id causes error
    private static final String PREFIX = "account:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AccountCache(RedisTemplate<String, Object> redisTemplate) {
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

    public void updateAccountFromCache(Long accountId, Account account) {
        String key = PREFIX + accountId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, account);
        } else {
            throw new IllegalArgumentException("Account with ID " + accountId + " does not exist in the cache.");
        }
    }

    public void deleteAccountFromCache(Long accountId) {
        redisTemplate.delete(PREFIX + accountId.toString());
    }

    public List<Account> getAllAccountsFromCache() {
        //Retrieve all keys from Redis
        Set<String> keys = redisTemplate.keys(PREFIX + "*");

        List<Account> accounts = null;
        if (keys != null) {
            accounts = new ArrayList<>();
            for (String key : keys) {
                // Retrieve the value associated with the key from Redis
                Account account = (Account) redisTemplate.opsForValue().get(key);
                if (account != null) {
                    accounts.add(account);
                }
            }
        }
        return accounts;
    }

    public Account getAccountFromCache(Long accountId) {
        return (Account) redisTemplate.opsForValue().get(PREFIX + accountId.toString());
    }
}