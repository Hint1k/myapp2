package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.publisher.TransactionEventPublisher;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.util.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceImplTest {

    @InjectMocks
    private BalanceServiceImpl balanceService;

    @Mock
    private AccountRepository repository;

    @Mock
    private TransactionEventPublisher publisher;

    @Test
    public void testChangeBalance_shouldUpdateBalance_whenTransactionIsValid() {
        // Given
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(100));
        BigDecimal amount = BigDecimal.valueOf(50);
        TransactionType transactionType = TransactionType.DEPOSIT;
        Long transactionId = 1L;

        // When
        boolean result = balanceService.changeBalance(account, amount, transactionType, transactionId);

        // Then
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(150), account.getBalance());
        verify(repository, times(1)).save(account);
    }

    @Test
    public void testChangeBalance_shouldReturnFalse_whenInsufficientFunds() {
        // Given
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(50));
        BigDecimal amount = BigDecimal.valueOf(100);
        TransactionType transactionType = TransactionType.WITHDRAWAL;
        Long transactionId = 1L;

        // When
        boolean result = balanceService.changeBalance(account, amount, transactionType, transactionId);

        // Then
        assertFalse(result);
        verify(publisher, times(1)).publishTransactionFailedEvent(transactionId);
        verify(repository, never()).save(account);
    }

    @Test
    public void testReverseBalance_shouldReturnTrue_whenBalanceReversedSuccessfully() {
        // Given
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(100));
        BigDecimal amount = BigDecimal.valueOf(50);
        TransactionType transactionType = TransactionType.WITHDRAWAL;
        Long transactionId = 1L;

        // When
        boolean result = balanceService.reverseBalance(account, amount, transactionType, transactionId);

        // Then
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(150), account.getBalance());
        verify(repository, times(1)).save(account);
    }

    @Test
    public void testMakeTransfer_shouldReturnTrue_whenTransferSuccessful() {
        // Given
        Account source = new Account();
        source.setBalance(BigDecimal.valueOf(200));

        Account destination = new Account();
        destination.setBalance(BigDecimal.valueOf(100));

        BigDecimal amount = BigDecimal.valueOf(50);
        Long transactionId = 1L;

        // When
        boolean result = balanceService.makeTransfer(source, destination, amount, transactionId);

        // Then
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(150), source.getBalance());
        assertEquals(BigDecimal.valueOf(150), destination.getBalance());
        verify(repository, times(2)).save(any(Account.class));
    }

    @Test
    public void testMakeTransfer_shouldReturnFalse_whenSourceHasInsufficientFunds() {
        // Given
        Account source = new Account();
        source.setBalance(BigDecimal.valueOf(30));

        Account destination = new Account();
        destination.setBalance(BigDecimal.valueOf(100));

        BigDecimal amount = BigDecimal.valueOf(50);
        Long transactionId = 1L;

        // When
        boolean result = balanceService.makeTransfer(source, destination, amount, transactionId);

        // Then
        assertFalse(result);
        verify(publisher, times(1)).publishTransactionFailedEvent(transactionId);
        verify(repository, never()).save(any(Account.class));
    }

    @Test
    public void testReverseTransfer_shouldReturnTrue_whenReverseTransferSuccessful() {
        // Given
        Account source = new Account();
        source.setBalance(BigDecimal.valueOf(200)); // Initial balance before reverse transfer

        Account destination = new Account();
        destination.setBalance(BigDecimal.valueOf(100)); // Initial balance before reverse transfer

        BigDecimal amount = BigDecimal.valueOf(50); // money that were transferred in the original transaction
        Long transactionId = 1L;

        System.out.println("Before reverseTransfer: Source Balance = " + source.getBalance() +
                ", Destination Balance = " + destination.getBalance());

        // When
        boolean result = balanceService.reverseTransfer(source, destination, amount, transactionId);

        System.out.println("After reverseTransfer: Source Balance = " + source.getBalance() +
                ", Destination Balance = " + destination.getBalance());

        // Then
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(250), source.getBalance()); // Source gets 50 back
        assertEquals(BigDecimal.valueOf(50), destination.getBalance()); // Destination loses 50 back
        verify(repository, times(2)).save(any(Account.class));
    }

    @Test
    public void testCalculateBalance_shouldReturnCorrectBalanceForDeposit() {
        // Given
        BigDecimal currentBalance = BigDecimal.valueOf(100);
        BigDecimal amount = BigDecimal.valueOf(50);

        // When
        BigDecimal result = balanceService.calculateBalance(currentBalance, amount, TransactionType.DEPOSIT);

        // Then
        assertEquals(BigDecimal.valueOf(150), result);
    }

    @Test
    public void testCalculateBalance_shouldReturnCorrectBalanceForWithdrawal() {
        // Given
        BigDecimal currentBalance = BigDecimal.valueOf(100);
        BigDecimal amount = BigDecimal.valueOf(50);

        // When
        BigDecimal result = balanceService.calculateBalance(currentBalance, amount, TransactionType.WITHDRAWAL);

        // Then
        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    public void testCalculateBalance_shouldReturnNegativeOne_whenInsufficientFunds() {
        // Given
        BigDecimal currentBalance = BigDecimal.valueOf(50);
        BigDecimal amount = BigDecimal.valueOf(100);

        // When
        BigDecimal result = balanceService.calculateBalance(currentBalance, amount, TransactionType.WITHDRAWAL);

        // Then
        assertEquals(BigDecimal.valueOf(-1), result);
    }

    @Test
    public void testCalculateBalance_shouldReturnNegativeTwo_whenInvalidTransactionType() {
        // Given
        BigDecimal currentBalance = BigDecimal.valueOf(100);
        BigDecimal amount = BigDecimal.valueOf(50);

        // When
        BigDecimal result = balanceService.calculateBalance(currentBalance, amount, null);

        // Then
        assertEquals(BigDecimal.valueOf(-2), result);
    }

    @Test
    public void testHasBalanceProblems_shouldReturnTrue_whenInsufficientFunds() {
        // Given
        Account account = new Account();
        Long transactionId = 1L;

        // When
        boolean result = balanceService.hasBalanceProblems(BigDecimal.valueOf(-1), account, transactionId);

        // Then
        assertTrue(result);
        verify(publisher, times(1)).publishTransactionFailedEvent(transactionId);
    }

    @Test
    public void testHasBalanceProblems_shouldReturnTrue_whenInvalidTransactionType() {
        // Given
        Long transactionId = 1L;

        // When
        boolean result = balanceService.hasBalanceProblems(BigDecimal.valueOf(-2), new Account(), transactionId);

        // Then
        assertTrue(result);
        verify(publisher, times(1)).publishTransactionFailedEvent(transactionId);
    }

    @Test
    public void testHasBalanceProblems_shouldReturnFalse_whenBalanceIsValid() {
        // Given
        Long transactionId = 1L;

        // When
        boolean result = balanceService.hasBalanceProblems(BigDecimal.valueOf(100), new Account(), transactionId);

        // Then
        assertFalse(result);
    }

    @Test
    public void testGetAccountFromDatabase_shouldReturnAccount_whenAccountExists() {
        // Given
        Account account = new Account();
        when(repository.findAccountByAccountNumber(123L)).thenReturn(account);

        // When
        Account result = balanceService.getAccountFromDatabase(123L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(account, result);
    }

    @Test
    public void testGetAccountFromDatabase_shouldReturnNull_whenAccountDoesNotExist() {
        // Given
        when(repository.findAccountByAccountNumber(123L)).thenReturn(null);

        // When
        Account result = balanceService.getAccountFromDatabase(123L, 1L);

        // Then
        assertNull(result);
    }
}