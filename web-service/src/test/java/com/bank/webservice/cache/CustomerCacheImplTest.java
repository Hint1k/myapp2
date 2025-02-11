package com.bank.webservice.cache;

import com.bank.webservice.dto.Customer;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerCacheImplTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private CustomerCacheImpl customerCache;

    @Test
    public void testAddCustomerToCache_Success() {
        // Create a customer mock
        Customer customer = createCustomers().stream().findFirst().orElse(null);
        assert customer != null : "Customer should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock the set method to prevent NPE and verify it does nothing
        doNothing().when(valueOpsMock).set(eq("customer:1"), eq(customer));

        // Perform the add operation
        customerCache.addCustomerToCache(1L, customer);

        // Verify RedisTemplate set method is called once
        verify(valueOpsMock, times(1)).set(eq("customer:1"), eq(customer));
    }

    @Test
    public void testAddAllCustomersToCache_Success() {
        // Create a list of customers
        List<Customer> customers = createCustomers();

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock the set method for each customer
        doNothing().when(valueOpsMock).set(eq("customer:1"), eq(customers.get(0)));
        doNothing().when(valueOpsMock).set(eq("customer:2"), eq(customers.get(1)));

        // Perform the add operation
        customerCache.addAllCustomersToCache(customers);

        // Verify RedisTemplate set method is called for each customer
        verify(valueOpsMock, times(1)).set(eq("customer:1"), eq(customers.get(0)));
        verify(valueOpsMock, times(1)).set(eq("customer:2"), eq(customers.get(1)));
    }

    @Test
    public void testUpdateCustomerInCache_Success() {
        // Create a customer mock
        Customer customer = createCustomers().stream().findFirst().orElse(null);
        assert customer != null : "Customer should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock RedisTemplate to return true when checking for the key
        when(redisTemplate.hasKey("customer:1")).thenReturn(true);

        // Perform the update operation
        customerCache.updateCustomerInCache(1L, customer);

        // Verify RedisTemplate set method is called once
        verify(valueOpsMock, times(1)).set(eq("customer:1"), eq(customer));
    }

    @Test
    public void testDeleteCustomerFromCache_Success() {
        // Perform the delete operation
        customerCache.deleteCustomerFromCache(1L);

        // Verify RedisTemplate delete method is called once
        verify(redisTemplate, times(1)).delete(eq("customer:1"));
    }

    @Test
    public void testGetAllCustomersFromCache_Success() {
        // Create a set of keys and mock CacheService to return some customers
        Set<String> keys = Set.of("customer:1", "customer:2", "customer:3");
        List<Customer> customers = createCustomers();

        // Mock CacheService methods
        when(cacheService.getAllKeys("customer:")).thenReturn(keys);
        when(cacheService.getObjectsFromCache(keys, Customer.class)).thenReturn(customers);

        // Perform the get operation
        List<Customer> result = customerCache.getAllCustomersFromCache();

        // Verify that the cache service was called correctly
        verify(cacheService, times(1)).getAllKeys("customer:");
        verify(cacheService, times(1)).getObjectsFromCache(keys, Customer.class);

        // Verify the result
        assert result.size() == 3;
        assert result.containsAll(customers);
    }

    @Test
    public void testGetCustomerFromCache_Success() {
        // Create a customer mock
        Customer customer = createCustomers().stream().findFirst().orElse(null);
        assert customer != null : "Customer should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock RedisTemplate to return the customer
        when(valueOpsMock.get("customer:1")).thenReturn(customer);

        // Perform the get operation
        Customer result = customerCache.getCustomerFromCache(1L);

        // Verify that RedisTemplate's get method was called once with the correct key
        verify(valueOpsMock, times(1)).get(eq("customer:1"));

        // Verify the result
        assert result != null;
        assert result.equals(customer);
    }

    @Test
    public void testGetCustomerFromCacheByCustomerNumber_Success() {
        // Create a list of customers and a customer to find
        List<Customer> customers = createCustomers();
        Customer customer = customers.stream().skip(2).findFirst().orElse(null);
        assert customer != null : "Customer should not be null";

        // Mock CacheService to return the list of customers
        when(cacheService.getAllKeys("customer:")).thenReturn(Set.of("customer:1", "customer:2", "customer:3"));
        when(cacheService.getObjectsFromCache(any(), eq(Customer.class))).thenReturn(customers);

        Customer result = customerCache.getCustomerFromCacheByCustomerNumber(3L);

        // Verify the result
        assert result != null;
        assert result.equals(customer);
    }

    private List<Customer> createCustomers() {
        List<Customer> customerList = new ArrayList<>();
        customerList.add(new Customer(1L,1L, "John Doe", "j.doe@example.com",
                "+10101010101", "10 Downing, London, UK", "1"));
        customerList.add(new Customer(2L,2L, "Jane Smith", "j.smith@example.com",
                "+10101010102", "11 Downing, London, UK", "2,3"));
        customerList.add(new Customer(3L,3L, "Alex Black", "a.black@example.com",
                "+10101010103", "12 Downing, London, UK", "4,5"));
        return customerList;
    }
}