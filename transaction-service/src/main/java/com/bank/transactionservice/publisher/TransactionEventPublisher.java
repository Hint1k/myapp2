package com.bank.transactionservice.publisher;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.util.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionEventPublisher {

    void publishTransactionCreatedEvent(Transaction transaction);

    void publishTransactionUpdatedEvent(Transaction transaction, BigDecimal oldAmount,
                                        TransactionType oldTransactionType, Long oldSourceAccountNumber,
                                        Long oldDestinationAccountNumber);

    void publishTransactionDeletedEvent(Transaction transaction);

    void publishAllTransactionsEvent(List<Transaction> transactions);

    void publishTransactionDetailsEvent(Transaction transaction);
}