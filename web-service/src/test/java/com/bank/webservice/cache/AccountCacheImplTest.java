package com.bank.webservice.cache;

import com.bank.webservice.dto.Account;
import com.bank.webservice.service.CacheService;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.AccountType;
import com.bank.webservice.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountCacheImplTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private AccountCacheImpl accountCache;

    @Test
    public void testAddAccountToCache_Success() {
        // Create an account mock
        Account account = createAccounts().stream().findFirst().orElse(null);
        assert account != null : "Account should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock the set method to prevent NPE and verify it does nothing
        doNothing().when(valueOpsMock).set(eq("account:1"), eq(account));

        // Perform the add operation
        accountCache.addAccountToCache(1L, account);

        // Verify RedisTemplate set method is called once
        verify(valueOpsMock, times(1)).set(eq("account:1"), eq(account));
    }

    @Test
    public void testAddAllAccountsToCache_Success() {
        // Create a list of accounts
        List<Account> accounts = createAccounts();
        Account account1 = createAccounts().stream().findFirst().orElse(null);
        Account account2 = createAccounts().stream().skip(1).findFirst().orElse(null);
        assert account1 != null : "Account should not be null"; // to suppress warnings
        assert account2 != null : "Account should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock the set method to prevent NPE and verify it does nothing
        doNothing().when(valueOpsMock).set(eq("account:1"), eq(account1));
        doNothing().when(valueOpsMock).set(eq("account:2"), eq(account2));

        // Perform the add operation
        accountCache.addAllAccountsToCache(accounts);

        // Verify RedisTemplate set method is called for each account
        verify(valueOpsMock, times(1)).set(eq("account:1"), eq(account1));
        verify(valueOpsMock, times(1)).set(eq("account:2"), eq(account2));
    }

    @Test
    public void testUpdateAccountInCache_Success() {
        // Create an account mock
        Account account = createAccounts().stream().findFirst().orElse(null);
        assert account != null : "Account should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock RedisTemplate to return true when checking for the key
        when(redisTemplate.hasKey("account:1")).thenReturn(true);

        // Perform the update operation
        accountCache.updateAccountInCache(1L, account);

        // Verify RedisTemplate set method is called once
        verify(valueOpsMock, times(1)).set(eq("account:1"), eq(account));
    }

    @Test
    public void testDeleteAccountFromCache_Success() {
        // Perform the delete operation
        accountCache.deleteAccountFromCache(1L);

        // Verify RedisTemplate delete method is called once
        verify(redisTemplate, times(1)).delete(eq("account:1"));
    }

    @Test
    public void testGetAllAccountsFromCache_Success() {
        // Create a set of keys and mock CacheService to return some accounts
        Set<String> keys = Set.of("account:1", "account:2", "account:3");
        List<Account> accounts = createAccounts();
        Account account1 = createAccounts().stream().findFirst().orElse(null);
        Account account2 = createAccounts().stream().skip(1).findFirst().orElse(null);
        Account account3 = createAccounts().stream().skip(2).findFirst().orElse(null);

        // Mock CacheService methods
        when(cacheService.getAllKeys("account:")).thenReturn(keys);
        when(cacheService.getObjectsFromCache(keys, Account.class)).thenReturn(accounts);

        // Perform the get operation
        List<Account> result = accountCache.getAllAccountsFromCache();

        // Verify that the cache service was called correctly
        verify(cacheService, times(1)).getAllKeys("account:");
        verify(cacheService, times(1)).getObjectsFromCache(keys, Account.class);

        // Verify the result
        assert result.size() == 3;
        assert result.contains(account1);
        assert result.contains(account2);
        assert result.contains(account3);
    }

    @Test
    public void testGetAccountFromCacheByAccountNumber_Success() {
        // Create a list of accounts
        List<Account> accounts = createAccounts();
        Account account1 = createAccounts().stream().findFirst().orElse(null);
        Account account2 = createAccounts().stream().skip(1).findFirst().orElse(null);

        // Mock CacheService to return the list of accounts
        when(cacheService.getAllKeys("account:")).thenReturn(Set.of("account:1", "account:2"));
        when(cacheService.getObjectsFromCache(any(), eq(Account.class))).thenReturn(accounts);

        // Perform the get operation by account number
        Account result1 = accountCache.getAccountFromCacheByAccountNumber(1L);
        Account result2 = accountCache.getAccountFromCacheByAccountNumber(2L);

        // Verify the result
        assert result1 != null;
        assert result2 != null;
        assert result1.equals(account1);
        assert result2.equals(account2);
    }

    @Test
    public void testGetAccountFromCache_Success() {
        // Create an account mock
        Account account = createAccounts().stream().findFirst().orElse(null);
        assert account != null : "Account should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock RedisTemplate to return the account
        when(redisTemplate.opsForValue().get("account:1")).thenReturn(account);

        // Perform the get operation
        Account result = accountCache.getAccountFromCache(1L);

        // Verify that RedisTemplate's get method was called once with the correct key
        verify(valueOpsMock, times(1)).get(eq("account:1"));

        // Verify the result
        assert result != null;
        assert result.equals(account);
    }

    @Test
    public void testGetAccountsFromCacheByCustomerNumber_Success() {
        // Create a list of accounts
        List<Account> accounts = createAccounts();
        Account account1 = createAccounts().stream().findFirst().orElse(null);
        Account account2 = createAccounts().stream().skip(2).findFirst().orElse(null);
        assert account1 != null : "Account should not be null"; // to suppress warnings
        assert account2 != null : "Account should not be null"; // to suppress warnings

        // Mock CacheService to return the list of accounts
        when(cacheService.getAllKeys("account:")).thenReturn(Set.of("account:1", "account:2"));
        when(cacheService.getObjectsFromCache(any(), eq(Account.class))).thenReturn(accounts);

        // Perform the get operation by customer number
        List<Account> result = accountCache.getAccountsFromCacheByCustomerNumber(1L);

        // Verify the result
        assert result.size() == 2;
        assert result.contains(account1);
        assert result.contains(account2);
    }

    private List<Account> createAccounts() {
        List<Account> accountList = new ArrayList<>();
        Account account1 = new Account(1L, 1L, new BigDecimal("100"), Currency.EUR,
                AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), 1L);
        Account account2 = new Account(2L, 2L, new BigDecimal("100"), Currency.EUR,
                AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), 2L);
        Account account3 = new Account(3L, 3L, new BigDecimal("100"), Currency.EUR,
                AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), 1L);
        accountList.add(account1);
        accountList.add(account2);
        accountList.add(account3);
        return accountList;
    }
}