package com.bank.webservice.cache;

import com.bank.webservice.dto.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedCache {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void addAccount(Long accountId, Account account) {
        redisTemplate.opsForValue().set(accountId.toString(), account);
    }

//    public Account getAccount(Long accountId) {
    public Account getAccount(Long accountNumber) {
//        return (Account) redisTemplate.opsForValue().get(accountId.toString());
        return (Account) redisTemplate.opsForValue().get(accountNumber.toString());
    }
}