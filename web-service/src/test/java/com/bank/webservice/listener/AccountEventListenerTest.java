package com.bank.webservice.listener;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.event.account.*;
import com.bank.webservice.service.LatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.bank.webservice.util.AccountStatus.ACTIVE;
import static com.bank.webservice.util.AccountType.SAVINGS;
import static com.bank.webservice.util.Currency.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountEventListenerTest {

    @Mock
    private LatchService latchService;

    @Mock
    private AccountCache accountCache;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private AccountEventListener accountEventListener;

    private AccountCreatedEvent accountCreatedEvent;
    private AccountUpdatedEvent accountUpdatedEvent;
    private AccountDeletedEvent accountDeletedEvent;
    private AllAccountsEvent allAccountsEvent;
    private AccountDetailsEvent accountDetailsEvent;

    @BeforeEach
    void setUp() {
        // Initialize test events
        accountCreatedEvent = new AccountCreatedEvent(
                1L, 1L, new BigDecimal("100"), USD, SAVINGS,
                ACTIVE, LocalDate.now(), 1L
        );

        accountUpdatedEvent = new AccountUpdatedEvent(
                1L, 1L, new BigDecimal("200"), USD, SAVINGS,
                ACTIVE, LocalDate.now(), 1L);

        accountDeletedEvent = new AccountDeletedEvent(1L, 1L);

        allAccountsEvent = new AllAccountsEvent(List.of(
                new Account(1L, 1L, new BigDecimal("100"), USD, SAVINGS,
                        ACTIVE, LocalDate.now(), 1L),
                new Account(2L, 2L, new BigDecimal("200"), USD, SAVINGS,
                        ACTIVE, LocalDate.now(), 2L)
        ));

        accountDetailsEvent = new AccountDetailsEvent(
                1L, 1L, new BigDecimal("200"), USD, SAVINGS,
                ACTIVE, LocalDate.now(), 1L
        );
    }

    @Test
    public void testHandleAccountCreatedEvent() {
        // Mock the cache behavior
        doNothing().when(accountCache).addAccountToCache(eq(1L), any(Account.class));

        // Act
        accountEventListener.handleAccountCreatedEvent(accountCreatedEvent, acknowledgment);

        // Assertions
        assertNotNull(accountCreatedEvent, "AccountCreatedEvent should not be null");
        assertEquals(1L, accountCreatedEvent.getAccountId(), "Account ID should be 1");

        // Verify
        verify(accountCache, times(1)).addAccountToCache(eq(1L), any(Account.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleAccountUpdatedEvent() {
        // Mock the cache behavior
        doNothing().when(accountCache).updateAccountInCache(eq(1L), any(Account.class));

        // Act
        accountEventListener.handleAccountUpdatedEvent(accountUpdatedEvent, acknowledgment);

        // Assertions
        assertNotNull(accountUpdatedEvent, "AccountUpdatedEvent should not be null");
        assertEquals(1L, accountUpdatedEvent.getAccountId(), "Account ID should be 1");
        assertEquals(new BigDecimal("200"), accountUpdatedEvent.getBalance(), "Balance should be 200");

        // Verify
        verify(accountCache, times(1)).updateAccountInCache(eq(1L), any(Account.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleAccountDeletedEvent() {
        // Mock the cache behavior
        doNothing().when(accountCache).deleteAccountFromCache(eq(1L));

        // Act
        accountEventListener.handleAccountDeletedEvent(accountDeletedEvent, acknowledgment);

        // Assertions
        assertNotNull(accountDeletedEvent, "AccountDeletedEvent should not be null");
        assertEquals(1L, accountDeletedEvent.getAccountId(), "Account ID should be 1");

        // Verify
        verify(accountCache, times(1)).deleteAccountFromCache(eq(1L));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleAllAccountsEvent() {
        // Mock the latch
        when(latchService.getLatch()).thenReturn(new CountDownLatch(1));

        // Mock the cache behavior
        doNothing().when(accountCache).addAllAccountsToCache(eq(allAccountsEvent.getAccounts()));

        // Act
        accountEventListener.handleAllAccountsEvent(allAccountsEvent, acknowledgment);

        // Assertions
        assertNotNull(allAccountsEvent, "AllAccountsEvent should not be null");
        assertEquals(2, allAccountsEvent.getAccounts().size(),
                "There should be 2 accounts in the event");

        // Verify
        verify(accountCache, times(1)).addAllAccountsToCache(eq(allAccountsEvent.getAccounts()));
        verify(acknowledgment, times(1)).acknowledge();
        verify(latchService, times(1)).getLatch();
    }

    @Test
    public void testHandleAccountDetailsEvent() {
        // Mock the cache behavior
        doNothing().when(accountCache).addAccountToCache(eq(1L), any(Account.class));

        // Act
        accountEventListener.handleAccountDetailsEvent(accountDetailsEvent, acknowledgment);

        // Assertions
        assertNotNull(accountDetailsEvent, "AccountDetailsEvent should not be null");
        assertEquals(1L, accountDetailsEvent.getAccountId(), "Account ID should be 1");
        assertEquals(new BigDecimal("200"), accountDetailsEvent.getBalance(), "Balance should be 200");

        // Verify
        verify(accountCache, times(1)).addAccountToCache(eq(1L), any(Account.class));
        verify(acknowledgment, times(1)).acknowledge();
    }
}