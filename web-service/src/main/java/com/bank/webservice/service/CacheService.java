package com.bank.webservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

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
}