package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.publisher.AccountEventPublisher;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.util.AccountStatus;
import com.bank.accountservice.util.AccountType;
import com.bank.accountservice.util.Currency;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountEventPublisher accountEventPublisher;

    @Test
    public void testSaveAccount() {
        // Given: A new account object
        Account account = new Account(12345L, BigDecimal.valueOf(1000), Currency.USD,
                AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDate.now(), 67890L);
        when(accountRepository.save(account)).thenReturn(account);

        // When: Saving the account
        accountService.saveAccount(account);

        // Then: Verify that the account is saved and an event is published
        verify(accountRepository, times(1)).save(account);
        verify(accountEventPublisher, times(1)).publishAccountCreatedEvent(account);
    }

    @Test
    public void testUpdateAccount() {
        // Given: An existing account object
        Account account = new Account(1L, 12345L, BigDecimal.valueOf(2000), Currency.USD,
                AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), 67890L);

        // When: Updating the account
        accountService.updateAccount(account);

        // Then: Verify that the account is updated and an event is published
        verify(accountRepository, times(1)).save(account);
        verify(accountEventPublisher, times(1)).publishAccountUpdatedEvent(account);
    }

    @Test
    public void testDeleteAccount_AccountExists() {
        // Given: An account exists in the repository
        Long accountId = 1L;
        Account account = new Account(accountId, 12345L, BigDecimal.valueOf(2000), Currency.USD,
                AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDate.now(), 67890L);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When: Deleting the account
        accountService.deleteAccount(accountId);

        // Then: Verify that the account is deleted and an event is published
        verify(accountRepository, times(1)).deleteById(accountId);
        verify(accountEventPublisher, times(1))
                .publishAccountDeletedEvent(accountId, account.getAccountNumber());
    }

    @Test
    public void testDeleteAccount_AccountNotFound() {
        // Given: The account does not exist in the repository
        Long accountId = 1L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // When & Then: Expect an EntityNotFoundException when trying to delete a non-existent account
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> accountService.deleteAccount(accountId));

        assertEquals("Account with id " + accountId + " not found", exception.getMessage());
        verify(accountRepository, never()).deleteById(accountId);
        verify(accountEventPublisher, never()).publishAccountDeletedEvent(any(), any());
    }

    @Test
    public void testFindAllAccounts() {
        // Given: A list of accounts exists
        List<Account> accounts = List.of(
                new Account(1L, 12345L, BigDecimal.valueOf(1000), Currency.USD,
                        AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDate.now(), 67890L),
                new Account(2L, 67890L, BigDecimal.valueOf(2000), Currency.EUR,
                        AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), 12345L)
        );
        when(accountRepository.findAll()).thenReturn(accounts);

        // When: Retrieving all accounts
        List<Account> result = accountService.findAllAccounts();

        // Then: Verify that all accounts are retrieved and an event is published
        assertEquals(accounts, result);
        verify(accountRepository, times(1)).findAll();
        verify(accountEventPublisher, times(1)).publishAllAccountsEvent(accounts);
    }

    @Test
    public void testFindAccountById_AccountExists() {
        // Given: An account exists in the repository
        Long accountId = 1L;
        Account account = new Account(accountId, 12345L, BigDecimal.valueOf(1000), Currency.USD,
                AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDate.now(), 67890L);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When: Retrieving the account by ID
        Account result = accountService.findAccountById(accountId);

        // Then: Verify that the correct account is retrieved and an event is published
        assertEquals(account, result);
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountEventPublisher, times(1)).publishAccountDetailsEvent(account);
    }

    @Test
    public void testFindAccountById_AccountNotFound() {
        // Given: The account does not exist in the repository
        Long accountId = 1L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // When & Then: Expect an EntityNotFoundException when retrieving a non-existent account
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> accountService.findAccountById(accountId));

        assertEquals("Account with id " + accountId + " not found", exception.getMessage());
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountEventPublisher, never()).publishAccountDetailsEvent(any());
    }
}