package com.bank.webservice.cache;

import com.bank.webservice.dto.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountDetailsCache {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void addAccountDetails(Long accountId, Account account) {
        redisTemplate.opsForValue().set(accountId.toString(), account);
    }

    public Account getAccountDetails(Long accountId) {
        return (Account) redisTemplate.opsForValue().get(accountId.toString());
    }
}