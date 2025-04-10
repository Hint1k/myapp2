package com.bank.transactionservice.publisher;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.event.transaction.*;
import com.bank.transactionservice.util.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventPublisherImpl implements TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishTransactionCreatedEvent(Transaction transaction) {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getAccountSourceNumber(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-created", event);
        log.info("Published transaction-created event for transaction id: {}", event.getTransactionId());
    }

    @Override
    public void publishTransactionUpdatedEvent(Transaction transaction, BigDecimal oldAmount,
                                               TransactionType oldTransactionType, Long oldSourceAccountNumber,
                                               Long oldDestinationAccountNumber) {
        TransactionUpdatedEvent event = new TransactionUpdatedEvent(
                transaction.getTransactionId(),
                oldAmount,
                transaction.getAmount(), // new amount
                transaction.getTransactionTime(),
                oldTransactionType,
                transaction.getTransactionType(), // new transaction type
                transaction.getTransactionStatus(),
                oldSourceAccountNumber,
                transaction.getAccountSourceNumber(), // new source account
                oldDestinationAccountNumber,
                transaction.getAccountDestinationNumber() // new destination account
        );
        kafkaTemplate.send("transaction-updated", event);
        log.info("Published transaction-updated event for transaction id: {}", event.getTransactionId());
    }

    @Override
    public void publishTransactionDeletedEvent(Transaction transaction) {
        TransactionDeletedEvent event = new TransactionDeletedEvent(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getAccountSourceNumber(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-deleted", event);
        log.info("Published transaction-deleted event for transaction id: {}", event.getTransactionId());
    }

    @Override
    public void publishAllTransactionsEvent(List<Transaction> transactions) {
        AllTransactionsEvent event = new AllTransactionsEvent(transactions);
        kafkaTemplate.send("all-transactions-received", event);
        log.info("Published all-transactions-received event with {} transactions", transactions.size());
    }

    @Override
    public void publishTransactionDetailsEvent(Transaction transaction) {
        TransactionDetailsEvent event = new TransactionDetailsEvent(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getTransactionTime(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getAccountSourceNumber(),
                transaction.getAccountDestinationNumber()
        );
        kafkaTemplate.send("transaction-details-received", event);
        log.info("Published transaction-details-received event for transaction id: {}", event.getTransactionId());
    }
}