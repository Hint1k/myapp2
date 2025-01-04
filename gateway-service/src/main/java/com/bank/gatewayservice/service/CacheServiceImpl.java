package com.bank.gatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CacheServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> List<T> getObjectsFromCache(Set<String> keys, Class<T> clazz) {
        List<T> objects = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                // Retrieve the value associated with the key from Redis
                T object = clazz.cast(redisTemplate.opsForValue().get(key));
                if (object != null) {
                    objects.add(object);
                }
            }
        }
        return objects;
    }

    @Override
    public Set<String> getAllKeys(String prefix) {
        Set<String> allKeys = new HashSet<>();
        // * = single digit, ** = double digits and so on.
        for (String pattern : new String[]{prefix + "*", prefix + "**", prefix + "***"}) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null) {
                allKeys.addAll(keys);
            }
        }
        return allKeys;
    }
}