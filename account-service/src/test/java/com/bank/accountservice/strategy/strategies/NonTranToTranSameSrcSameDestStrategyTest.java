package com.bank.accountservice.strategy.strategies;

import com.bank.accountservice.data.TestData;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.util.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NonTranToTranSameSrcSameDestStrategyTest {

    @Mock
    private BalanceService service;

    @InjectMocks
    private NonTranToTranSameSrcSameDestStrategy strategy;

    private Long oldAccountSourceNumber;
    private Long newAccountSourceNumber;
    private Long oldAccountDestinationNumber;
    private Long newAccountDestinationNumber;
    private BigDecimal oldAmount;
    private BigDecimal newAmount;
    private TransactionType oldTransactionType;
    private TransactionType newTransactionType;
    private Long transactionId;
    private Account oldSourceAccount;
    private Account newDestinationAccount;

    @BeforeEach
    void setUp() {
        // Initialize test data
        TestData testData = new TestData();
        oldAccountSourceNumber = testData.oldAccountSourceNumber;
        newAccountSourceNumber = testData.newAccountSourceNumber;
        oldAccountDestinationNumber = testData.oldAccountDestinationNumber;
        newAccountDestinationNumber = testData.newAccountDestinationNumber;
        oldAmount = testData.oldAmount;
        newAmount = testData.newAmount;
        oldTransactionType = testData.oldTransactionType;
        newTransactionType = testData.newTransactionType;
        transactionId = testData.transactionId;
        oldSourceAccount = testData.oldSourceAccount;
        newDestinationAccount = testData.newDestinationAccount;
    }

    @Test
    public void testExecute_shouldSuccessfullyReverseBalanceAndMakeTransfer_whenValidAccountsAndAmount() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountDestinationNumber, transactionId))
                .thenReturn(newDestinationAccount);
        when(service.reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId))
                .thenReturn(true);
        when(service.makeTransfer(oldSourceAccount, newDestinationAccount, newAmount, transactionId))
                .thenReturn(true);

        // When
        strategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType,
                newTransactionType, transactionId);

        // Then
        verify(service, times(1)).getAccountFromDatabase(oldAccountSourceNumber, transactionId);
        verify(service, times(1))
                .getAccountFromDatabase(newAccountDestinationNumber, transactionId);
        verify(service, times(1))
                .reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId);
        verify(service, times(1))
                .makeTransfer(oldSourceAccount, newDestinationAccount, newAmount, transactionId);
    }

    @Test
    public void testExecute_shouldThrowException_whenOldSourceAccountNotFound() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(null);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                strategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not find an account with id: " + oldAccountSourceNumber, exception.getMessage());
    }

    @Test
    public void testExecute_shouldThrowException_whenNewDestinationAccountNotFound() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountDestinationNumber, transactionId)).thenReturn(null);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                strategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not find an account with id: " +
                newAccountDestinationNumber, exception.getMessage());
    }

    @Test
    public void testExecute_shouldThrowException_whenBalanceReversalFails() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountDestinationNumber, transactionId))
                .thenReturn(newDestinationAccount);
        when(service.reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId))
                .thenReturn(false);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                strategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not reverse a transaction with id: " + transactionId, exception.getMessage());
    }

    @Test
    public void testExecute_shouldThrowException_whenTransferFails() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountDestinationNumber, transactionId))
                .thenReturn(newDestinationAccount);
        when(service.reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId))
                .thenReturn(true);
        when(service.makeTransfer(oldSourceAccount, newDestinationAccount, newAmount, transactionId))
                .thenReturn(false);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                strategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not make a transaction with id: " + transactionId, exception.getMessage());
    }
}