package com.bank.webservice.service.impl;

import com.bank.webservice.dto.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CacheServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private CacheServiceImpl cacheService;

    @Test
    public void testGetObjectsFromCache_Success() {
        // Create a set of keys
        Set<String> keys = Set.of("user:1", "user:2", "user:3");

        // Create mock users
        List<User> users = createUsers();

        // Mock RedisTemplate's opsForValue().get() behavior
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);
        when(valueOpsMock.get("user:1")).thenReturn(users.get(0));
        when(valueOpsMock.get("user:2")).thenReturn(users.get(1));
        when(valueOpsMock.get("user:3")).thenReturn(users.get(2));

        // Perform the get operation
        List<User> result = cacheService.getObjectsFromCache(keys, User.class);

        // Verify RedisTemplate get method is called for each key
        verify(valueOpsMock, times(1)).get("user:1");
        verify(valueOpsMock, times(1)).get("user:2");
        verify(valueOpsMock, times(1)).get("user:3");

        // Verify the result
        assert result.size() == 3;
        assert result.containsAll(users);
    }

    @Test
    public void testGetObjectsFromCache_WithNullKeys() {
        // Perform the get operation with null keys
        List<User> result = cacheService.getObjectsFromCache(null, User.class);

        // Verify the result is an empty list
        assert result.isEmpty();
    }

    @Test
    public void testGetAllKeys_Success() {
        // Mock RedisTemplate's keys() method behavior
        when(redisTemplate.keys("user:*")).thenReturn(Set.of("user:1", "user:2"));
        when(redisTemplate.keys("user:**")).thenReturn(Set.of("user:3"));
        when(redisTemplate.keys("user:***")).thenReturn(Set.of());

        // Perform the get operation
        Set<String> result = cacheService.getAllKeys("user:");

        // Verify RedisTemplate keys() method is called with each pattern
        verify(redisTemplate, times(1)).keys("user:*");
        verify(redisTemplate, times(1)).keys("user:**");
        verify(redisTemplate, times(1)).keys("user:***");

        // Verify the result
        assert result.size() == 3;
        assert result.containsAll(Set.of("user:1", "user:2", "user:3"));
    }

    @Test
    public void testGetAllKeys_WhenNoKeysFound() {
        // Mock RedisTemplate's keys() method to return empty sets
        when(redisTemplate.keys("user:*")).thenReturn(Set.of());
        when(redisTemplate.keys("user:**")).thenReturn(Set.of());
        when(redisTemplate.keys("user:***")).thenReturn(Set.of());

        // Perform the get operation
        Set<String> result = cacheService.getAllKeys("user:");

        // Verify RedisTemplate keys() method is called with each pattern
        verify(redisTemplate, times(1)).keys("user:*");
        verify(redisTemplate, times(1)).keys("user:**");
        verify(redisTemplate, times(1)).keys("user:***");

        // Verify the result is an empty set
        assert result.isEmpty();
    }

    private List<User> createUsers() {
        List<User> userList = new ArrayList<>();
        userList.add(new User(1L, 101L, "john doe", "password123"));
        userList.add(new User(2L, 102L, "jane smith", "securepass"));
        userList.add(new User(3L, 103L, "alex black", "mypassword"));
        return userList;
    }
}