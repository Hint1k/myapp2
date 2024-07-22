package com.bank.accountservice.publisher;

public interface TransactionEventPublisher {

    void publishTransactionFailedEvent(Long transactionId);

    void publishTransactionApprovedEvent(Long transactionId);
}