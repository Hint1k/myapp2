package com.bank.webservice.cache;

import com.bank.webservice.dto.User;
import com.bank.webservice.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserCacheImpl implements UserCache {

    // objects of different classes with the same id in cache cause errors
    private static final String PREFIX = "user:";
    private final CacheService service;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addUserToCache(Long userId, User user) {
        redisTemplate.opsForValue().set(PREFIX + userId.toString(), user);
    }

    @Override
    public void addAllUsersToCache(List<User> users) {
        for (User user : users) {
            redisTemplate.opsForValue().set(PREFIX + user.getUserId().toString(), user);
        }
    }

    @Override
    public void updateUserInCache(Long userId, User user) {
        String key = PREFIX + userId.toString();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, user);
        } else {
            log.error("User with id {} not found", userId);
        }
    }

    @Override
    public void deleteUserFromCache(Long userId) {
        redisTemplate.delete(PREFIX + userId.toString());
    }

    @Override
    public List<User> getAllUsersFromCache() {
        Set<String> allKeys = service.getAllKeys(PREFIX);
        return service.getObjectsFromCache(allKeys, User.class);
    }

    @Override
    public User getUserFromCache(Long userId) {
        return (User) redisTemplate.opsForValue().get(PREFIX + userId.toString());
    }
}