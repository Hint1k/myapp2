package com.bank.accountservice.service.impl;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountEventPublisher accountEventPublisher;

    @Test
    public void testUpdateAccountDueToCustomerChange_RemoveAndAssignCustomerNumber() {
        // Given: A customer with an existing account and new account numbers to assign
        Long customerNumber = 67890L;
        String accountNumbers = "12345,67890";

        Account existingAccount = new Account(1L, 11111L, BigDecimal.valueOf(1000),
                Currency.USD, AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDate.now(), customerNumber);
        Account newAccount1 = new Account(2L, 12345L, BigDecimal.valueOf(2000),
                Currency.USD, AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), null);
        Account newAccount2 = new Account(3L, 67890L, BigDecimal.valueOf(3000),
                Currency.EUR, AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDate.now(), null);

        when(accountRepository.findAccountByCustomerNumber(customerNumber)).thenReturn(existingAccount);
        when(accountRepository.findAccountByAccountNumber(12345L)).thenReturn(newAccount1);
        when(accountRepository.findAccountByAccountNumber(67890L)).thenReturn(newAccount2);

        // When: Updating account due to customer change
        customerService.updateAccountDueToCustomerChange(customerNumber, accountNumbers);

        // Then: Verify that the customer number is removed from the old account and assigned to new accounts
        assertEquals(0L, existingAccount.getCustomerNumber());
        assertEquals(AccountStatus.INACTIVE, existingAccount.getAccountStatus());
        verify(accountRepository, times(1)).save(existingAccount);
        verify(accountEventPublisher, times(1)).publishAccountUpdatedEvent(existingAccount);

        assertEquals(customerNumber, newAccount1.getCustomerNumber());
        verify(accountRepository, times(1)).save(newAccount1);
        verify(accountEventPublisher, times(1)).publishAccountDetailsEvent(newAccount1);

        assertEquals(customerNumber, newAccount2.getCustomerNumber());
        verify(accountRepository, times(1)).save(newAccount2);
        verify(accountEventPublisher, times(1)).publishAccountDetailsEvent(newAccount2);
    }

    @Test
    public void testUpdateAccountDueToCustomerChange_OnlyRemoveCustomerNumber() {
        // Given: A customer with an existing account and no new account numbers to assign
        Long customerNumber = 67890L;
        String accountNumbers = "";

        Account existingAccount = new Account(1L, 11111L, BigDecimal.valueOf(1000),
                Currency.USD, AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDate.now(), customerNumber);

        when(accountRepository.findAccountByCustomerNumber(customerNumber)).thenReturn(existingAccount);

        // When: Updating account due to customer change
        customerService.updateAccountDueToCustomerChange(customerNumber, accountNumbers);

        // Then: Verify that the customer number is removed and account status is set to inactive
        assertEquals(0L, existingAccount.getCustomerNumber());
        assertEquals(AccountStatus.INACTIVE, existingAccount.getAccountStatus());
        verify(accountRepository, times(1)).save(existingAccount);
        verify(accountEventPublisher, times(1)).publishAccountUpdatedEvent(existingAccount);
    }

    @Test
    public void testUpdateAccountDueToCustomerChange_OnlyAssignCustomerNumber() {
        // Given: No existing account, but new account numbers to assign
        Long customerNumber = 67890L;
        String accountNumbers = "12345,67890";

        Account newAccount1 = new Account(2L, 12345L, BigDecimal.valueOf(2000),
                Currency.USD, AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), null);
        Account newAccount2 = new Account(3L, 67890L, BigDecimal.valueOf(3000),
                Currency.EUR, AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDate.now(), null);

        when(accountRepository.findAccountByCustomerNumber(customerNumber)).thenReturn(null);
        when(accountRepository.findAccountByAccountNumber(12345L)).thenReturn(newAccount1);
        when(accountRepository.findAccountByAccountNumber(67890L)).thenReturn(newAccount2);

        // When: Updating account due to customer change
        customerService.updateAccountDueToCustomerChange(customerNumber, accountNumbers);

        // Then: Verify that customer number is assigned to new accounts
        assertEquals(customerNumber, newAccount1.getCustomerNumber());
        verify(accountRepository, times(1)).save(newAccount1);
        verify(accountEventPublisher, times(1)).publishAccountDetailsEvent(newAccount1);

        assertEquals(customerNumber, newAccount2.getCustomerNumber());
        verify(accountRepository, times(1)).save(newAccount2);
        verify(accountEventPublisher, times(1)).publishAccountDetailsEvent(newAccount2);
    }

    @Test
    public void testAssignCustomerNumberToAccount_AccountNotFound() {
        // Given: A non-existent account number
        Long customerNumber = 67890L;
        String accountNumbers = "12345";

        when(accountRepository.findAccountByAccountNumber(12345L)).thenReturn(null);

        // When & Then: Expect an EntityNotFoundException when assigning a customer number to a non-existent account
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> customerService.updateAccountDueToCustomerChange(customerNumber, accountNumbers));

        assertEquals("Account with account number 12345 not found", exception.getMessage());
        verify(accountRepository, times(1)).findAccountByAccountNumber(12345L);
        verify(accountRepository, never()).save(any());
        verify(accountEventPublisher, never()).publishAccountDetailsEvent(any());
    }
}