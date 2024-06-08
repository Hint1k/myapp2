package com.bank.webservice.cache;

import com.bank.webservice.dto.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class AccountCache {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AccountCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addAccountToCache(Long accountId, Account account) {
        redisTemplate.opsForValue().set(accountId.toString(), account);
    }

    public void addAllAccountsToCache(List<Account> accounts) {
        for (Account account : accounts) {
            redisTemplate.opsForValue().set(account.getAccountId().toString(), account);
        }
    }

    public void updateAccountFromCache(Long accountId, Account account) {
        String key = accountId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, account);
        } else {
            throw new IllegalArgumentException("Account with ID " + accountId + " does not exist in the cache.");
        }
    }

    public void deleteAccountFromCache(Long accountId) {
        redisTemplate.delete(accountId.toString());
    }

    public List<Account> getAllAccountsFromCache() {
        //Retrieve all keys from Redis
        Set<String> keys = redisTemplate.keys("*");

        List<Account> accounts = new ArrayList<>();
        if (keys != null) {
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
        return (Account) redisTemplate.opsForValue().get(accountId.toString());
    }
}