package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.publisher.TransactionEventPublisher;
import com.bank.transactionservice.repository.TransactionRepository;
import com.bank.transactionservice.util.TransactionStatus;
import com.bank.transactionservice.util.TransactionType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEventPublisher transactionEventPublisher;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction(
                1L,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                TransactionType.DEPOSIT,
                TransactionStatus.PENDING,
                123456L,
                654321L
        );
    }

    @Test
    public void testSaveTransaction_Success() {
        // Given: A valid transaction
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // When: Calling saveTransaction
        transactionService.saveTransaction(transaction);

        // Then: Transaction should be saved and an event should be published
        verify(transactionRepository, times(1)).save(transaction);
        verify(transactionEventPublisher, times(1)).publishTransactionCreatedEvent(transaction);
    }

    @Test
    public void testUpdateTransaction_Success() {
        // Given: An existing transaction in the database
        when(transactionRepository.findById(transaction.getTransactionId()))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // When: Calling updateTransaction
        transactionService.updateTransaction(transaction);

        // Then: Transaction should be updated and an event should be published
        verify(transactionRepository, times(1)).save(transaction);
        verify(transactionEventPublisher, times(1)).publishTransactionUpdatedEvent(
                eq(transaction), any(BigDecimal.class), any(TransactionType.class), any(Long.class), any(Long.class));
    }

    @Test
    public void testUpdateTransaction_NotFound() {
        // Given: No transaction exists with the given ID
        when(transactionRepository.findById(transaction.getTransactionId()))
                .thenReturn(Optional.empty());

        // When & Then: Expecting an exception
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> transactionService.updateTransaction(transaction));
        assertEquals("Transaction with id 1 not found", exception.getMessage());

        verify(transactionRepository, never()).save(any());
        verify(transactionEventPublisher, never()).publishTransactionUpdatedEvent(any(), any(), any(), any(), any());
    }

    @Test
    public void testDeleteTransaction_Success() {
        // Given: A transaction exists
        when(transactionRepository.findById(transaction.getTransactionId()))
                .thenReturn(Optional.of(transaction));

        // When: Calling deleteTransaction
        transactionService.deleteTransaction(transaction.getTransactionId());

        // Then: Transaction should be deleted and an event should be published
        verify(transactionRepository, times(1)).deleteById(transaction.getTransactionId());
        verify(transactionEventPublisher, times(1)).publishTransactionDeletedEvent(transaction);
    }

    @Test
    public void testDeleteTransaction_NotFound() {
        // Given: No transaction exists
        when(transactionRepository.findById(transaction.getTransactionId()))
                .thenReturn(Optional.empty());

        // When & Then: Expecting an exception
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> transactionService.deleteTransaction(transaction.getTransactionId()));
        assertEquals("Transaction with id 1 not found", exception.getMessage());

        verify(transactionRepository, never()).deleteById(any());
        verify(transactionEventPublisher, never()).publishTransactionDeletedEvent(any());
    }

    @Test
    public void testFindTransactionById_Success() {
        // Given: A transaction exists
        when(transactionRepository.findById(transaction.getTransactionId()))
                .thenReturn(Optional.of(transaction));

        // When: Calling findTransactionById
        Transaction foundTransaction = transactionService.findTransactionById(transaction.getTransactionId());

        // Then: Correct transaction should be returned
        assertEquals(transaction, foundTransaction);
        verify(transactionEventPublisher, times(1)).publishTransactionDetailsEvent(transaction);
    }

    @Test
    public void testFindTransactionById_NotFound() {
        // Given: No transaction exists
        when(transactionRepository.findById(transaction.getTransactionId()))
                .thenReturn(Optional.empty());

        // When & Then: Expecting an exception
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> transactionService.findTransactionById(transaction.getTransactionId()));
        assertEquals("Transaction with id 1 not found", exception.getMessage());

        verify(transactionEventPublisher, never()).publishTransactionDetailsEvent(any());
    }

    @Test
    public void testHandleTransactionFailure_Success() {
        // Given: A transaction exists
        when(transactionRepository.findById(transaction.getTransactionId()))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // When: Calling handleTransactionFailure
        transactionService.handleTransactionFailure(transaction.getTransactionId());

        // Then: The transaction should be marked as failed
        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        verify(transactionRepository, times(1)).save(transaction);
        verify(transactionEventPublisher, times(1)).publishTransactionDetailsEvent(transaction);
    }

    @Test
    public void testHandleTransactionApproval_Success() {
        // Given: A transaction exists
        when(transactionRepository.findById(transaction.getTransactionId()))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // When: Calling handleTransactionApproval
        transactionService.handleTransactionApproval(transaction.getTransactionId());

        // Then: The transaction should be marked as approved
        assertEquals(TransactionStatus.APPROVED, transaction.getTransactionStatus());
        verify(transactionRepository, times(1)).save(transaction);
        verify(transactionEventPublisher, times(1)).publishTransactionDetailsEvent(transaction);
    }

    @Test
    public void testFreezeTransactions_Success() {
        // Given: A list of transactions exist
        List<Transaction> transactions = List.of(transaction);
        when(transactionRepository.findTransactionsByAccountNumber(transaction.getAccountSourceNumber()))
                .thenReturn(transactions);

        // When: Calling freezeTransactions
        transactionService.freezeTransactions(transaction.getAccountSourceNumber());

        // Then: Transactions should be marked as frozen
        assertEquals(TransactionStatus.FROZEN, transactions.getFirst().getTransactionStatus());
        verify(transactionRepository, times(1)).save(transaction);
        verify(transactionEventPublisher, times(1)).publishAllTransactionsEvent(transactions);
    }

    @Test
    public void testSuspendTransactions_Success() {
        // Given: A list of transactions exist
        List<Transaction> transactions = List.of(transaction);
        when(transactionRepository.findTransactionsByAccountNumber(transaction.getAccountSourceNumber()))
                .thenReturn(transactions);

        // When: Calling suspendTransactions
        transactionService.suspendOrUnsuspendTransactions(transaction.getAccountSourceNumber(), "suspend");

        // Then: Transactions should be marked as suspended
        assertEquals(TransactionStatus.SUSPENDED, transactions.getFirst().getTransactionStatus());
        verify(transactionRepository, times(1)).save(transaction);
        verify(transactionEventPublisher, times(1)).publishAllTransactionsEvent(transactions);
    }

    @Test
    public void testUnsuspendTransactions_Success() {
        // Given: A list of transactions exist
        List<Transaction> transactions = List.of(transaction);
        when(transactionRepository.findTransactionsByAccountNumber(transaction.getAccountSourceNumber()))
                .thenReturn(transactions);

        // When: Calling unsuspendTransactions
        transactionService.suspendOrUnsuspendTransactions(transaction.getAccountSourceNumber(), "unsuspend");

        // Then: Transactions should be marked as approved
        assertEquals(TransactionStatus.APPROVED, transactions.getFirst().getTransactionStatus());
        verify(transactionRepository, times(1)).save(transaction);
        verify(transactionEventPublisher, times(1)).publishAllTransactionsEvent(transactions);
    }
}