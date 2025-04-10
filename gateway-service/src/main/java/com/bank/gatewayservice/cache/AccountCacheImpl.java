package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountCacheImpl implements AccountCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "account:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Account getAccountFromCache(Long accountId) {
        return (Account) redisTemplate.opsForValue().get(PREFIX + accountId.toString());
    }
}