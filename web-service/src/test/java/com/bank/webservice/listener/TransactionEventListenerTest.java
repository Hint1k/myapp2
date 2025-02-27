package com.bank.webservice.listener;

import com.bank.webservice.cache.TransactionCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.transaction.*;
import com.bank.webservice.service.LatchService;
import com.bank.webservice.util.TransactionStatus;
import com.bank.webservice.util.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionEventListenerTest {

    @Mock
    private LatchService latchService;

    @Mock
    private TransactionCache transactionCache;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private TransactionEventListener transactionEventListener;

    private TransactionCreatedEvent transactionCreatedEvent;
    private TransactionUpdatedEvent transactionUpdatedEvent;
    private TransactionDeletedEvent transactionDeletedEvent;
    private AllTransactionsEvent allTransactionsEvent;
    private TransactionDetailsEvent transactionDetailsEvent;

    @BeforeEach
    void setUp() {
        // Initialize test events
        transactionCreatedEvent = new TransactionCreatedEvent(
                1L, new BigDecimal("100.00"), LocalDateTime.now(),
                TransactionType.TRANSFER, TransactionStatus.PENDING,
                2L, 1L
        );

        transactionUpdatedEvent = new TransactionUpdatedEvent(
                1L, new BigDecimal("200.00"), LocalDateTime.now(),
                TransactionType.TRANSFER, TransactionStatus.APPROVED,
                2L, 1L
        );

        transactionDeletedEvent = new TransactionDeletedEvent(1L);

        allTransactionsEvent = new AllTransactionsEvent(List.of(
                new Transaction(1L, new BigDecimal("100.00"), LocalDateTime.now(),
                        TransactionType.TRANSFER, TransactionStatus.PENDING, 2L,
                        1L),
                new Transaction(2L, new BigDecimal("200.00"), LocalDateTime.now(),
                        TransactionType.TRANSFER, TransactionStatus.APPROVED, 1L,
                        2L)
        ));

        transactionDetailsEvent = new TransactionDetailsEvent(
                1L, new BigDecimal("100.00"), LocalDateTime.now(),
                TransactionType.TRANSFER, TransactionStatus.PENDING,
                2L, 1L
        );
    }

    @Test
    public void testHandleTransactionCreatedEvent() {
        // Mock the cache behavior (void method)
        doNothing().when(transactionCache).addTransactionToCache(eq(1L), any(Transaction.class));

        // Act
        transactionEventListener.handleTransactionCreatedEvent(transactionCreatedEvent, acknowledgment);

        // Assertions
        assertNotNull(transactionCreatedEvent, "TransactionCreatedEvent should not be null");
        assertEquals(1L, transactionCreatedEvent.getTransactionId(), "Transaction ID should be 1");

        // Verify
        verify(transactionCache, times(1))
                .addTransactionToCache(eq(1L), any(Transaction.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleTransactionUpdatedEvent() {
        // Mock the cache behavior (void method)
        doNothing().when(transactionCache).updateTransactionFromCache(eq(1L), any(Transaction.class));

        // Act
        transactionEventListener.handleTransactionUpdatedEvent(transactionUpdatedEvent, acknowledgment);

        // Assertions
        assertNotNull(transactionUpdatedEvent, "TransactionUpdatedEvent should not be null");
        assertEquals(1L, transactionUpdatedEvent.getTransactionId(), "Transaction ID should be 1");
        assertEquals(new BigDecimal("200.00"), transactionUpdatedEvent.getAmount(),
                "Amount should be 200.00");

        // Verify
        verify(transactionCache, times(1))
                .updateTransactionFromCache(eq(1L), any(Transaction.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleTransactionDeletedEvent() {
        // Mock the cache behavior (void method)
        doNothing().when(transactionCache).deleteTransactionFromCache(eq(1L));

        // Act
        transactionEventListener.handleTransactionDeletedEvent(transactionDeletedEvent, acknowledgment);

        // Assertions
        assertNotNull(transactionDeletedEvent, "TransactionDeletedEvent should not be null");
        assertEquals(1L, transactionDeletedEvent.getTransactionId(), "Transaction ID should be 1");

        // Verify
        verify(transactionCache, times(1)).deleteTransactionFromCache(eq(1L));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleAllTransactionsEvent() {
        // Mock the latch
        when(latchService.getLatch()).thenReturn(new CountDownLatch(1));

        // Mock the cache behavior (void method)
        doNothing().when(transactionCache).addAllTransactionsToCache(eq(allTransactionsEvent.getTransactions()));

        // Act
        transactionEventListener.handleAllTransactionsEvent(allTransactionsEvent, acknowledgment);

        // Assertions
        assertNotNull(allTransactionsEvent, "AllTransactionsEvent should not be null");
        assertEquals(2, allTransactionsEvent.getTransactions().size(),
                "There should be 2 transactions in the event");

        // Verify
        verify(transactionCache, times(1))
                .addAllTransactionsToCache(eq(allTransactionsEvent.getTransactions()));
        verify(acknowledgment, times(1)).acknowledge();
        verify(latchService, times(1)).getLatch();
    }

    @Test
    public void testHandleTransactionDetailsEvent() {
        // Mock the cache behavior (void method)
        doNothing().when(transactionCache).addTransactionToCache(eq(1L), any(Transaction.class));

        // Act
        transactionEventListener.handleTransactionDetailsEvent(transactionDetailsEvent, acknowledgment);

        // Assertions
        assertNotNull(transactionDetailsEvent, "TransactionDetailsEvent should not be null");
        assertEquals(1L, transactionDetailsEvent.getTransactionId(), "Transaction ID should be 1");
        assertEquals(new BigDecimal("100.00"), transactionDetailsEvent.getAmount(),
                "Amount should be 100.00");

        // Verify
        verify(transactionCache, times(1))
                .addTransactionToCache(eq(1L), any(Transaction.class));
        verify(acknowledgment, times(1)).acknowledge();
    }
}