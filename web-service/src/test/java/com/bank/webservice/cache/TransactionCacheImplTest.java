package com.bank.webservice.cache;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.service.CacheService;
import com.bank.webservice.util.TransactionStatus;
import com.bank.webservice.util.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionCacheImplTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private TransactionCacheImpl transactionCache;

    @Test
    public void testAddTransactionToCache_Success() {
        // Create a transaction mock
        Transaction transaction = createTransactions().stream().findFirst().orElse(null);
        assert transaction != null : "Transaction should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock the set method to prevent NPE and verify it does nothing
        doNothing().when(valueOpsMock).set(eq("transaction:1"), eq(transaction));

        // Perform the add operation
        transactionCache.addTransactionToCache(1L, transaction);

        // Verify RedisTemplate set method is called once
        verify(valueOpsMock, times(1)).set(eq("transaction:1"), eq(transaction));
    }

    @Test
    public void testAddAllTransactionsToCache_Success() {
        // Create a list of transactions
        List<Transaction> transactions = createTransactions();

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock the set method for each transaction
        doNothing().when(valueOpsMock).set(eq("transaction:1"), eq(transactions.get(0)));
        doNothing().when(valueOpsMock).set(eq("transaction:2"), eq(transactions.get(1)));

        // Perform the add operation
        transactionCache.addAllTransactionsToCache(transactions);

        // Verify RedisTemplate set method is called for each transaction
        verify(valueOpsMock, times(1)).set(eq("transaction:1"), eq(transactions.get(0)));
        verify(valueOpsMock, times(1)).set(eq("transaction:2"), eq(transactions.get(1)));
    }

    @Test
    public void testUpdateTransactionFromCache_Success() {
        // Create a transaction mock
        Transaction transaction = createTransactions().stream().findFirst().orElse(null);
        assert transaction != null : "Transaction should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock RedisTemplate to return true when checking for the key
        when(redisTemplate.hasKey("transaction:1")).thenReturn(true);

        // Perform the update operation
        transactionCache.updateTransactionFromCache(1L, transaction);

        // Verify RedisTemplate set method is called once
        verify(valueOpsMock, times(1)).set(eq("transaction:1"), eq(transaction));
    }

    @Test
    public void testDeleteTransactionFromCache_Success() {
        // Perform the delete operation
        transactionCache.deleteTransactionFromCache(1L);

        // Verify RedisTemplate delete method is called once
        verify(redisTemplate, times(1)).delete(eq("transaction:1"));
    }

    @Test
    public void testGetAllTransactionsFromCache_Success() {
        // Create a set of keys and mock CacheService to return some transactions
        Set<String> keys = Set.of("transaction:1", "transaction:2", "transaction:3");
        List<Transaction> transactions = createTransactions();

        // Mock CacheService methods
        when(cacheService.getAllKeys("transaction:")).thenReturn(keys);
        when(cacheService.getObjectsFromCache(keys, Transaction.class)).thenReturn(transactions);

        // Perform the get operation
        List<Transaction> result = transactionCache.getAllTransactionsFromCache();

        // Verify that the cache service was called correctly
        verify(cacheService, times(1)).getAllKeys("transaction:");
        verify(cacheService, times(1)).getObjectsFromCache(keys, Transaction.class);

        // Verify the result
        assert result.size() == 3;
        assert result.containsAll(transactions);
    }

    @Test
    public void testGetTransactionFromCache_Success() {
        // Create a transaction mock
        Transaction transaction = createTransactions().stream().findFirst().orElse(null);
        assert transaction != null : "Transaction should not be null"; // to suppress warnings

        // Mock the behavior of RedisTemplate's opsForValue()
        ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        // Mock RedisTemplate to return the transaction
        when(valueOpsMock.get("transaction:1")).thenReturn(transaction);

        // Perform the get operation
        Transaction result = transactionCache.getTransactionFromCache(1L);

        // Verify that RedisTemplate's get method was called once with the correct key
        verify(valueOpsMock, times(1)).get(eq("transaction:1"));

        // Verify the result
        assert result != null;
        assert result.equals(transaction);
    }

    @Test
    public void testGetTransactionsForAccountFromCache_Success() {
        // Create a list of transactions
        List<Transaction> transactions = createTransactions();

        // Mock CacheService to return the list of transactions
        when(cacheService.getAllKeys("transaction:")).thenReturn(Set.of("transaction:2", "transaction:3"));
        when(cacheService.getObjectsFromCache(any(), eq(Transaction.class))).thenReturn(transactions);

        // Perform the get operation by account number
        List<Transaction> result = transactionCache.getTransactionsForAccountFromCache(2L);

        // Verify the result
        assert result.size() == 2;
        assert result.contains(transactions.get(1));
        assert result.contains(transactions.get(2));
    }

    @Test
    public void testGetTransactionsForMultipleAccountsFromCache_Success() {
        // Create a list of transactions
        List<Transaction> transactions = createTransactions();

        // Mock CacheService to return the list of transactions
        when(cacheService.getAllKeys("transaction:"))
                .thenReturn(Set.of("transaction:1", "transaction:2", "transaction:3"));
        when(cacheService.getObjectsFromCache(any(), eq(Transaction.class))).thenReturn(transactions);

        // Perform the get operation by multiple account numbers
        List<Transaction> result = transactionCache.getTransactionsForMultipleAccountsFromCache(List.of(1L, 2L, 3L));

        // Verify the result
        assert result.size() == 3;
        assert result.containsAll(transactions);
    }

    private List<Transaction> createTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction(1L, new BigDecimal("100"), LocalDateTime.now(),
                TransactionType.DEPOSIT, TransactionStatus.APPROVED, 1L, 1L));
        transactionList.add(new Transaction(2L, new BigDecimal("100"), LocalDateTime.now(),
                TransactionType.DEPOSIT, TransactionStatus.APPROVED, 2L, 2L));
        transactionList.add(new Transaction(3L, new BigDecimal("100"), LocalDateTime.now(),
                TransactionType.DEPOSIT, TransactionStatus.APPROVED, 2L, 2L));
        return transactionList;
    }
}