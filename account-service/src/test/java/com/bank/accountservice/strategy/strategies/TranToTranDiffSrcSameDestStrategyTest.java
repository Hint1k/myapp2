package com.bank.accountservice.strategy.strategies;

import com.bank.accountservice.data.TestData;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.util.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TranToTranDiffSrcSameDestStrategyTest {

    @Mock
    private BalanceService service;

    @InjectMocks
    private TranToTranDiffSrcSameDestStrategy strategy;

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
    private Account newSourceAccount;
    private Account oldDestinationAccount;

    @BeforeEach
    void setup() {
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
        newSourceAccount = testData.newSourceAccount;
        oldDestinationAccount = testData.oldDestinationAccount;
    }

    @Test
    public void execute_shouldSuccessfullyReverseAndMakeNewTransfer_whenValidAccountsAndAmounts() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(newSourceAccount);
        when(service.getAccountFromDatabase(oldAccountDestinationNumber, transactionId))
                .thenReturn(oldDestinationAccount);
        when(service.reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId))
                .thenReturn(true);
        when(service.makeTransfer(newSourceAccount, oldDestinationAccount, newAmount, transactionId))
                .thenReturn(true);

        // When
        strategy.execute(oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType, newTransactionType,
                transactionId);

        // Then
        verify(service, times(1)).getAccountFromDatabase(oldAccountSourceNumber, transactionId);
        verify(service, times(1)).getAccountFromDatabase(newAccountSourceNumber, transactionId);
        verify(service, times(1))
                .getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
        verify(service, times(1))
                .reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
        verify(service, times(1))
                .makeTransfer(newSourceAccount, oldDestinationAccount, newAmount, transactionId);
    }

    @Test
    public void execute_shouldThrowException_whenOldSourceAccountNotFound() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(null);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                strategy.execute(oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                        newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType, newTransactionType,
                        transactionId)
        );

        assertEquals("Could not find an account with id: " + oldAccountSourceNumber, exception.getMessage());
    }

    @Test
    public void execute_shouldThrowException_whenNewSourceAccountNotFound() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(null);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                strategy.execute(oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                        newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType, newTransactionType,
                        transactionId)
        );

        assertEquals("Could not find an account with id: " + newAccountSourceNumber, exception.getMessage());
    }

    @Test
    public void execute_shouldThrowException_whenOldDestinationAccountNotFound() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(newSourceAccount);
        when(service.getAccountFromDatabase(oldAccountDestinationNumber, transactionId)).thenReturn(null);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                strategy.execute(oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                        newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType, newTransactionType,
                        transactionId)
        );

        assertEquals("Could not find an account with id: " +
                oldAccountDestinationNumber, exception.getMessage());
    }

    @Test
    public void execute_shouldThrowException_whenTransferReversalFails() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(newSourceAccount);
        when(service.getAccountFromDatabase(oldAccountDestinationNumber, transactionId))
                .thenReturn(oldDestinationAccount);
        when(service.reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId))
                .thenReturn(false);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                strategy.execute(oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                        newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType, newTransactionType,
                        transactionId)
        );

        assertEquals("Could not reverse a transaction with id: " + transactionId, exception.getMessage());
    }

    @Test
    public void execute_shouldThrowException_whenNewTransferFails() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(newSourceAccount);
        when(service.getAccountFromDatabase(oldAccountDestinationNumber, transactionId))
                .thenReturn(oldDestinationAccount);
        when(service.reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId))
                .thenReturn(true);
        when(service.makeTransfer(newSourceAccount, oldDestinationAccount, newAmount, transactionId))
                .thenReturn(false);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                strategy.execute(oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                        newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType, newTransactionType,
                        transactionId)
        );

        assertEquals("Could not make a transaction with id: " + transactionId, exception.getMessage());
    }
}