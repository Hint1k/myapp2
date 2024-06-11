package com.bank.transactionservice.listener;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.event.*;
import com.bank.transactionservice.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionEventListener {

    private final TransactionService transactionService;

    @Autowired
    public TransactionEventListener(TransactionService transactionService
    ) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "transaction-creation-requested", groupId = "transaction-service")
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received transaction-creation-requested event for account destination number: {}",
                event.getAccountDestinationNumber());
        try {
            Transaction transaction = new Transaction(
                    event.getAmount(),
                    event.getTransactionTime(),
                    event.getTransactionType(),
                    event.getTransactionStatus(),
                    event.getAccountDestinationNumber()
            );
            transactionService.saveTransaction(transaction);
            acknowledgment.acknowledge();
        } catch (Exception exception) {
            log.error("Error saving transaction: {}", exception.getMessage());
            // TODO handle exception here later
        }
    }

    @KafkaListener(topics = "transaction-update-requested", groupId = "transaction-service")
    public void handleTransactionUpdatedEvent(TransactionUpdatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received transaction-update-requested event for transaction id: {}", event.getTransactionId());
        Transaction transaction = new Transaction(
                event.getTransactionId(),
                event.getAmount(),
                event.getTransactionTime(),
                event.getTransactionType(),
                event.getTransactionStatus(),
                event.getAccountDestinationNumber()
        );
        transactionService.updateTransaction(transaction);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "transaction-deletion-requested", groupId = "transaction-service")
    public void handleTransactionDeletedEvent(TransactionDeletedEvent event, Acknowledgment acknowledgment) {
        Long transactionId = event.getTransactionId();
        log.info("Received transaction-deletion-requested event for transaction id: {}", transactionId);
        try {
            transactionService.deleteTransaction(transactionId);
            acknowledgment.acknowledge();
        } catch (Exception exception) {
            log.error("Error deleting transaction: {}", exception.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "all-transactions-requested", groupId = "transaction-service")
    public void handleAllTransactionsEvent(Acknowledgment acknowledgment) {
        log.info("Received all-transactions-requested event");
        try {
            transactionService.findAllTransactions();
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error finding all transactions: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "account-transactions-requested", groupId = "transaction-service")
    public void handleAccountTransactionsEvent(AccountTransactionsEvent event, Acknowledgment acknowledgment) {
        Long accountNumber = event.getAccountNumber();
        log.info("Received account-transactions-requested event for account number: {}", accountNumber);
        try {
            transactionService.findAccountTransactions(accountNumber);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error finding transactions by account number {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "transaction-details-requested", groupId = "transaction-service")
    public void handleTransactionDetailsEvent(TransactionDetailsEvent event, Acknowledgment acknowledgment) {
        Long transactionId = event.getTransactionId();
        log.info("Received transaction-details-requested event for transaction id: {}", transactionId);
        try {
            transactionService.findTransactionById(transactionId);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error finding transaction by id: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "transaction-failed", groupId = "transaction-service")
    public void handleTransactionFailedEvent(TransactionFailedEvent event, Acknowledgment acknowledgment) {
        Long transactionId = event.getTransactionId();
        log.info("Received transaction-failed event for transaction id: {}", transactionId);
        try {
            transactionService.handleTransactionFailure(transactionId);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error handling transaction failure {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "transaction-approved", groupId = "transaction-service")
    public void handleTransactionApprovedEvent(TransactionApprovedEvent event, Acknowledgment acknowledgment) {
        Long transactionId = event.getTransactionId();
        log.info("Received transaction-approved event for transaction id: {}", transactionId);
        try {
            transactionService.handleTransactionApproval(transactionId);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error handling transaction approval {}", e.getMessage());
            // TODO implement error handling later
        }
    }
}