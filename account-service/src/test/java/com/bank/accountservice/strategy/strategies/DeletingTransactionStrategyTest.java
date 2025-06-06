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
public class DeletingTransactionStrategyTest {

    @Mock
    private BalanceService service;

    @InjectMocks
    private DeletingTransactionStrategy deletingTransactionStrategy;

    private Long oldAccountSourceNumber;
    private Long newAccountSourceNumber;
    private Long oldAccountDestinationNumber;
    private Long newAccountDestinationNumber;
    private BigDecimal oldAmount;
    private BigDecimal newAmount;
    private TransactionType oldTransactionType;
    private TransactionType newTransactionType;
    private Long transactionId;
    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    public void setUp() {
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
        sourceAccount = testData.newSourceAccount;
        destinationAccount = testData.newDestinationAccount;
    }

    @Test
    public void testExecute_shouldSuccessfullyReverseTransfer_whenValidAccountsAndAmount() {
        // Given
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(sourceAccount);
        when(service.getAccountFromDatabase(newAccountDestinationNumber, transactionId)).thenReturn(destinationAccount);
        when(service.reverseTransfer(sourceAccount, destinationAccount, newAmount, transactionId)).thenReturn(true);

        // When
        deletingTransactionStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType,
                newTransactionType, transactionId);

        // Then
        verify(service, times(1)).getAccountFromDatabase(newAccountSourceNumber, transactionId);
        verify(service, times(1))
                .getAccountFromDatabase(newAccountDestinationNumber, transactionId);
        verify(service, times(1))
                .reverseTransfer(sourceAccount, destinationAccount, newAmount, transactionId);
    }

    @Test
    public void testExecute_shouldThrowException_whenSourceAccountNotFound() {
        // Given
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(null);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                deletingTransactionStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not find an account with id: " + newAccountSourceNumber, exception.getMessage());
    }

    @Test
    public void testExecute_shouldThrowException_whenDestinationAccountNotFound() {
        // Given
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(sourceAccount);
        when(service.getAccountFromDatabase(newAccountDestinationNumber, transactionId)).thenReturn(null);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                deletingTransactionStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not find an account with id: " +
                newAccountDestinationNumber, exception.getMessage());
    }

    @Test
    public void testExecute_shouldThrowException_whenTransferReversalFails() {
        // Given
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(sourceAccount);
        when(service.getAccountFromDatabase(newAccountDestinationNumber, transactionId)).thenReturn(destinationAccount);
        when(service.reverseTransfer(sourceAccount, destinationAccount, newAmount, transactionId)).thenReturn(false);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                deletingTransactionStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not make a transaction with id: " + transactionId, exception.getMessage());
    }

    @Test
    public void testExecute_shouldSuccessfullyReverseBalance_whenDepositTransaction() {
        // Given
        newTransactionType = TransactionType.DEPOSIT;
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(sourceAccount);
        when(service.reverseBalance(sourceAccount, newAmount, newTransactionType, transactionId)).thenReturn(true);

        // When
        deletingTransactionStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType,
                newTransactionType, transactionId);

        // Then
        verify(service, times(1)).getAccountFromDatabase(newAccountSourceNumber, transactionId);
        verify(service, times(1))
                .reverseBalance(sourceAccount, newAmount, newTransactionType, transactionId);
    }

    @Test
    public void testExecute_shouldThrowException_whenDepositReversalFails() {
        // Given
        newTransactionType = TransactionType.DEPOSIT;
        when(service.getAccountFromDatabase(newAccountSourceNumber, transactionId)).thenReturn(sourceAccount);
        when(service.reverseBalance(sourceAccount, newAmount, newTransactionType, transactionId)).thenReturn(false);

        // When & Then
        TransactionProcessingException exception = assertThrows(TransactionProcessingException.class, () ->
                deletingTransactionStrategy.execute(oldAccountSourceNumber, newAccountSourceNumber,
                        oldAccountDestinationNumber, newAccountDestinationNumber, oldAmount, newAmount,
                        oldTransactionType, newTransactionType, transactionId)
        );

        assertEquals("Could not make a transaction with id: " + transactionId, exception.getMessage());
    }
}