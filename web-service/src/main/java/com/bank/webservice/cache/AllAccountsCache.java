package com.bank.webservice.cache;

import com.bank.webservice.dto.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AllAccountsCache {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void addAllAccounts(List<Account> accounts) {
        for (Account account : accounts) {
            redisTemplate.opsForValue().set(account.getId().toString(), account);
        }
    }

    public List<Account> getAllAccounts() {
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
}