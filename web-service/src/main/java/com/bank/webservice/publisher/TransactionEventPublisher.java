package com.bank.webservice.publisher;

import com.bank.webservice.dto.Transaction;

public interface TransactionEventPublisher {

    void publishTransactionCreatedEvent(Transaction transaction);

    void publishTransactionUpdatedEvent(Transaction transaction);

    void publishTransactionDeletedEvent(Long transactionId);

    void publishAllTransactionsEvent();

    void publishTransactionDetailsEvent(Long transactionId);
}