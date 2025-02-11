package com.bank.webservice.cache;
import com.bank.webservice.dto.User;
import com.bank.webservice.service.CacheService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCacheImplTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private UserCacheImpl userCache;

    @Test
    public void testAddUserToCache_Success() {
        // Create a user mock
        User user = createUsers().stream().findFirst().orElse(null);
        assert user != null : "User should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock the set method to prevent NPE and verify it does nothing
        doNothing().when(valueOpsMock).set(eq("user:1"), eq(user));

        // Perform the add operation
        userCache.addUserToCache(1L, user);

        // Verify RedisTemplate set method is called once
        verify(valueOpsMock, times(1)).set(eq("user:1"), eq(user));
    }

    @Test
    public void testAddAllUsersToCache_Success() {
        // Create a list of users
        List<User> users = createUsers();

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock the set method for each user
        doNothing().when(valueOpsMock).set(eq("user:1"), eq(users.get(0)));
        doNothing().when(valueOpsMock).set(eq("user:2"), eq(users.get(1)));

        // Perform the add operation
        userCache.addAllUsersToCache(users);

        // Verify RedisTemplate set method is called for each user
        verify(valueOpsMock, times(1)).set(eq("user:1"), eq(users.get(0)));
        verify(valueOpsMock, times(1)).set(eq("user:2"), eq(users.get(1)));
    }

    @Test
    public void testUpdateUserInCache_Success() {
        // Create a user mock
        User user = createUsers().stream().findFirst().orElse(null);
        assert user != null : "User should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock RedisTemplate to return true when checking for the key
        when(redisTemplate.hasKey("user:2")).thenReturn(true);

        // Perform the update operation
        userCache.updateUserInCache(2L, user);

        // Verify RedisTemplate set method is called once
        verify(valueOpsMock, times(1)).set(eq("user:2"), eq(user));
    }

    @Test
    public void testDeleteUserFromCache_Success() {
        // Perform the delete operation
        userCache.deleteUserFromCache(3L);

        // Verify RedisTemplate delete method is called once
        verify(redisTemplate, times(1)).delete(eq("user:3"));
    }

    @Test
    public void testGetAllUsersFromCache_Success() {
        // Create a set of keys and mock CacheService to return some users
        Set<String> keys = Set.of("user:1", "user:2", "user:3");
        List<User> users = createUsers();

        // Mock CacheService methods
        when(cacheService.getAllKeys("user:")).thenReturn(keys);
        when(cacheService.getObjectsFromCache(keys, User.class)).thenReturn(users);

        // Perform the get operation
        List<User> result = userCache.getAllUsersFromCache();

        // Verify that the cache service was called correctly
        verify(cacheService, times(1)).getAllKeys("user:");
        verify(cacheService, times(1)).getObjectsFromCache(keys, User.class);

        // Verify the result
        assert result.size() == 3;
        assert result.containsAll(users);
    }

    @Test
    public void testGetUserFromCache_Success() {
        // Create a user mock
        User user = createUsers().stream().findFirst().orElse(null);
        assert user != null : "User should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock RedisTemplate to return the user
        when(valueOpsMock.get("user:1")).thenReturn(user);

        // Perform the get operation
        User result = userCache.getUserFromCache(1L);

        // Verify that RedisTemplate's get method was called once with the correct key
        verify(valueOpsMock, times(1)).get(eq("user:1"));

        // Verify the result
        assert result != null;
        assert result.equals(user);
    }

    private List<User> createUsers() {
        List<User> userList = new ArrayList<>();
        userList.add(new User(1L, 1L, "john doe", "password123"));
        userList.add(new User(2L, 2L, "jane smith", "securepass"));
        userList.add(new User(3L, 3L, "alex black", "mypassword"));
        return userList;
    }
}