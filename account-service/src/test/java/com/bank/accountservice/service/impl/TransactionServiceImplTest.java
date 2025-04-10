package com.bank.accountservice.service.impl;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.publisher.AccountEventPublisher;
import com.bank.accountservice.publisher.TransactionEventPublisher;
import com.bank.accountservice.service.AccountService;
import com.bank.accountservice.strategy.TransactionUpdateContext;
import com.bank.accountservice.strategy.strategies.DeletingTransactionStrategy;
import com.bank.accountservice.strategy.strategies.NewTransactionCreationStrategy;
import com.bank.accountservice.strategy.strategies.TranToTranSameSrcSameDestStrategy;
import com.bank.accountservice.util.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionEventPublisher transactionPublisher;

    @Mock
    private AccountEventPublisher accountPublisher;

    @Mock
    private TransactionUpdateContext context;

    @Mock
    private AccountService accountService;

    @Test
    public void testUpdateAccountBalanceForTransactCreate_shouldExecuteStrategyAndPublishEvent_whenTransactSucceeds() {
        // Given
        Long accountSourceNumber = 1L;
        Long accountDestinationNumber = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);
        TransactionType transactionType = TransactionType.TRANSFER;
        Long transactionId = 10L;

        // When
        transactionService.updateAccountBalanceForTransactionCreate(
                accountSourceNumber, accountDestinationNumber, amount, transactionType, transactionId);

        // Then
        verify(context).setStrategy(any(NewTransactionCreationStrategy.class));
        verify(context).executeStrategy(
                isNull(), eq(accountSourceNumber), isNull(), eq(accountDestinationNumber),
                isNull(), eq(amount), isNull(), eq(transactionType), eq(transactionId));
        verify(transactionPublisher, times(1)).publishTransactionApprovedEvent(transactionId);
    }

    @Test
    public void testUpdateAccountBalanceForTransactionCreate_shouldPublishFailedEvent_whenTransactionFails() {
        // Given
        Long accountSourceNumber = 1L;
        Long accountDestinationNumber = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);
        TransactionType transactionType = TransactionType.TRANSFER;
        Long transactionId = 10L;

        doThrow(new TransactionProcessingException("Error")).when(context).executeStrategy(
                any(), any(), any(), any(), any(), any(), any(), any(), any());

        // When
        transactionService.updateAccountBalanceForTransactionCreate(
                accountSourceNumber, accountDestinationNumber, amount, transactionType, transactionId);

        // Then
        verify(transactionPublisher, times(1)).publishTransactionFailedEvent(transactionId);
    }

    @Test
    public void testUpdateAccountBalanceForTransactDelete_shouldExecuteStrategyAndPublishEvents_whenTransactSuccess() {
        // Given
        Long accountSourceNumber = 1L;
        Long accountDestinationNumber = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);
        TransactionType transactionType = TransactionType.TRANSFER;
        Long transactionId = 10L;

        Account sourceAccount = new Account();
        Account destinationAccount = new Account();

        when(accountService.findAccountById(accountSourceNumber)).thenReturn(sourceAccount);
        when(accountService.findAccountById(accountDestinationNumber)).thenReturn(destinationAccount);

        // Capture the accounts passed to publishAccountDetailsEvent
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        // When
        transactionService.updateAccountBalanceForTransactionDelete(
                accountSourceNumber, accountDestinationNumber, amount, transactionType, transactionId);

        // Then
        verify(context).setStrategy(any(DeletingTransactionStrategy.class));
        verify(context).executeStrategy(
                isNull(), eq(accountSourceNumber), isNull(), eq(accountDestinationNumber),
                isNull(), eq(amount), isNull(), eq(transactionType), eq(transactionId));

        // Capture the arguments passed to the method
        verify(accountPublisher, times(2)).publishAccountDetailsEvent(accountCaptor.capture());

        // Assert that the correct accounts were passed
        List<Account> capturedAccounts = accountCaptor.getAllValues();
        assertEquals(2, capturedAccounts.size());
        assertTrue(capturedAccounts.contains(sourceAccount));
        assertTrue(capturedAccounts.contains(destinationAccount));
    }

    @Test
    public void testUpdateAccountBalanceForTransactionDelete_shouldHandleException_whenTransactionFails() {
        // Given
        Long accountSourceNumber = 1L;
        Long accountDestinationNumber = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);
        TransactionType transactionType = TransactionType.TRANSFER;
        Long transactionId = 10L;

        // Mock behavior to throw an exception during strategy execution
        doThrow(new TransactionProcessingException("Error")).when(context).executeStrategy(
                any(), any(), any(), any(), any(), any(), any(), any(), any());

        // When
        transactionService.updateAccountBalanceForTransactionDelete(
                accountSourceNumber, accountDestinationNumber, amount, transactionType, transactionId);

        // Then
        // Verify that the transaction failed event was published
        verify(transactionPublisher, times(1)).publishTransactionFailedEvent(transactionId);
    }

    @Test
    public void testUpdateAccountBalanceForTransactionUpdate_shouldSelectCorrectStrategy_whenConditionsMatch() {
        // Given
        Long oldAccountSourceNumber = 1L;
        Long newAccountSourceNumber = 1L;
        Long oldAccountDestinationNumber = 2L;
        Long newAccountDestinationNumber = 2L;
        BigDecimal oldAmount = BigDecimal.valueOf(100);
        BigDecimal newAmount = BigDecimal.valueOf(150);
        TransactionType oldTransactionType = TransactionType.TRANSFER;
        TransactionType newTransactionType = TransactionType.TRANSFER;
        Long transactionId = 10L;

        // When
        transactionService.updateAccountBalanceForTransactionUpdate(
                oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType,
                newTransactionType, transactionId);

        // Then
        verify(context).setStrategy(any(TranToTranSameSrcSameDestStrategy.class));
        verify(context).executeStrategy(
                eq(oldAccountSourceNumber), eq(newAccountSourceNumber),
                eq(oldAccountDestinationNumber), eq(newAccountDestinationNumber),
                eq(oldAmount), eq(newAmount), eq(oldTransactionType), eq(newTransactionType),
                eq(transactionId));
        verify(transactionPublisher, times(1)).publishTransactionApprovedEvent(transactionId);
    }

    @Test
    public void testUpdateAccountBalanceForTransactionUpdate_shouldPublishFailedEvent_whenTransactionFails() {
        // Given
        Long oldAccountSourceNumber = 1L;
        Long newAccountSourceNumber = 1L;
        Long oldAccountDestinationNumber = 2L;
        Long newAccountDestinationNumber = 2L;
        BigDecimal oldAmount = BigDecimal.valueOf(100);
        BigDecimal newAmount = BigDecimal.valueOf(150);
        TransactionType oldTransactionType = TransactionType.TRANSFER;
        TransactionType newTransactionType = TransactionType.TRANSFER;
        Long transactionId = 10L;

        doThrow(new TransactionProcessingException("Error")).when(context).executeStrategy(
                any(), any(), any(), any(), any(), any(), any(), any(), any());

        // When
        transactionService.updateAccountBalanceForTransactionUpdate(
                oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType,
                newTransactionType, transactionId);

        // Then
        verify(transactionPublisher, times(1)).publishTransactionFailedEvent(transactionId);
    }
}