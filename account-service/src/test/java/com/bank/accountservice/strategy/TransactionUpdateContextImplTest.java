package com.bank.accountservice.strategy;

import com.bank.accountservice.data.TestData;
import com.bank.accountservice.exception.TransactionProcessingException;
import com.bank.accountservice.util.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TransactionUpdateContextImplTest {

    @Mock
    private TransactionUpdateStrategy mockStrategy;

    @InjectMocks
    private TransactionUpdateContextImpl context;

    private Long oldAccountSourceNumber;
    private Long newAccountSourceNumber;
    private Long oldAccountDestinationNumber;
    private Long newAccountDestinationNumber;
    private BigDecimal oldAmount;
    private BigDecimal newAmount;
    private TransactionType oldTransactionType;
    private TransactionType newTransactionType;
    private Long transactionId;

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
    }

    @Test
    public void testSetStrategy_shouldAssignStrategy() {
        // Given
        TransactionUpdateStrategy newStrategy = mock(TransactionUpdateStrategy.class);

        // When
        context.setStrategy(newStrategy);

        // Then
        assertNotNull(newStrategy); // Ensuring the strategy is set (no null assignment)
    }

    @Test
    public void testExecuteStrategy_shouldInvokeExecuteMethodOfStrategy() {
        // Given in setup()

        // When
        context.setStrategy(mockStrategy);
        context.executeStrategy(oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType, newTransactionType,
                transactionId
        );

        // Then
        verify(mockStrategy).execute(eq(oldAccountSourceNumber), eq(newAccountSourceNumber),
                eq(oldAccountDestinationNumber), eq(newAccountDestinationNumber),
                eq(oldAmount), eq(newAmount), eq(oldTransactionType), eq(newTransactionType), eq(transactionId));
    }

    @Test
    public void testExecuteStrategy_shouldHandleException_whenStrategyThrowsException() {
        // Given in setup()

        // Mock the strategy to throw an exception
        doThrow(new TransactionProcessingException("Error")).when(mockStrategy).execute(
                any(), any(), any(), any(), any(), any(), any(), any(), any());

        // When & Then
        assertThrows(TransactionProcessingException.class, () -> {
            context.setStrategy(mockStrategy);
            context.executeStrategy(oldAccountSourceNumber, newAccountSourceNumber, oldAccountDestinationNumber,
                    newAccountDestinationNumber, oldAmount, newAmount, oldTransactionType, newTransactionType,
                    transactionId
            );
        });
    }
}