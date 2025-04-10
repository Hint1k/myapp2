package com.bank.accountservice.strategy.strategies;

import com.bank.accountservice.data.TestData;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.util.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NonTranToNonTranDiffSrcStrategyTest {

    @Mock
    private BalanceService service;

    @InjectMocks
    private NonTranToNonTranDiffSrcStrategy nonTranToNonTranDiffSrcStrategy;

    private Long oldAccountSourceNumber;
    private Long newAccountSourceNumber;
    private Long oldAccountDestinationNumber;
    private Long newAccountDestinationNumber;
    private BigDecimal oldAmount;
    private BigDecimal newAmount;
    private TransactionType oldTransactionType;
    private TransactionType newTransactionType;
    private Long transactionId;
    private Account newSourceAccount;
    private Account oldSourceAccount;

    @BeforeEach
    void setUp() {
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
        newSourceAccount = testData.newSourceAccount;
        oldSourceAccount = testData.oldSourceAccount;
    }

    @Test
    public void testExecute_shouldSuccessfullyReverseBalanceAndChangeBalance_whenValidAccountsAndAmounts() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(newSourceAccount);
        when(service.reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId))
                .thenReturn(true);
        when(service.changeBalance(newSourceAccount, newAmount, newTransactionType, transactionId)).thenReturn(true);

        // When
        nonTranToNonTranDiffSrcStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType,
                newTransactionType, transactionId);

        // Then
        verify(service, times(1)).getAccountFromDatabase(oldAccountSourceNumber, transactionId);
        verify(service, times(1)).getAccountFromDatabase(newAccountSourceNumber, transactionId);
        verify(service, times(1))
                .reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId);
        verify(service, times(1))
                .changeBalance(newSourceAccount, newAmount, newTransactionType, transactionId);
    }

    @Test
    public void testExecute_shouldThrowException_whenOldSourceAccountNotFound() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(null);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                nonTranToNonTranDiffSrcStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not find an account with id: " + oldAccountSourceNumber, exception.getMessage());
    }

    @Test
    public void testExecute_shouldThrowException_whenNewSourceAccountNotFound() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(null);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                nonTranToNonTranDiffSrcStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not find an account with id: " + newAccountSourceNumber, exception.getMessage());
    }

    @Test
    public void testExecute_shouldThrowException_whenBalanceReversalFails() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(newSourceAccount);
        when(service.reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId))
                .thenReturn(false);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                nonTranToNonTranDiffSrcStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not reverse a transaction with id: " + transactionId, exception.getMessage());
    }

    @Test
    public void testExecute_shouldThrowException_whenBalanceChangeFails() {
        // Given
        when(service.getAccountFromDatabase(oldAccountSourceNumber, transactionId)).thenReturn(oldSourceAccount);
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(newSourceAccount);
        when(service.reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId))
                .thenReturn(true);
        when(service.changeBalance(newSourceAccount, newAmount, newTransactionType, transactionId))
                .thenReturn(false);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                nonTranToNonTranDiffSrcStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not make a transaction with id: " + transactionId, exception.getMessage());
    }
}