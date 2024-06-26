package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.publisher.TransactionEventPublisher;
import com.bank.transactionservice.repository.TransactionRepository;
import com.bank.transactionservice.util.TransactionStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionEventPublisher publisher;
    private final TransactionRepository repository;

    @Autowired
    public TransactionServiceImpl(TransactionEventPublisher publisher,
                                  TransactionRepository repository) {
        this.publisher = publisher;
        this.repository = repository;
    }

    @Override
    @Transactional
    public void saveTransaction(Transaction transaction) {
        Transaction savedTransaction = repository.save(transaction);
        publisher.publishTransactionCreatedEvent(savedTransaction);
        log.info("Transaction saved: {}", savedTransaction);
    }

    @Override
    @Transactional
    public void updateTransaction(Transaction transaction) {
        // JPA repository should merge instead of save
        repository.save(transaction);
        publisher.publishTransactionUpdatedEvent(transaction);
        log.info("Transaction with id: {} updated", transaction.getTransactionId());
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId) {
        repository.deleteById(transactionId);
        publisher.publishTransactionDeletedEvent(transactionId);
        log.info("Transaction with id: {} deleted", transactionId);
    }

    @Override
    @Transactional
    public List<Transaction> findAllTransactions() {
        List<Transaction> transactions = repository.findAll();
        publisher.publishAllTransactionsEvent(transactions);
        log.info("Retrieved {} transactions", transactions.size());
        return transactions;
    }

    @Override
    @Transactional
    public List<Transaction> findAccountTransactions(Long accountNumber) {
        List<Transaction> transactions = repository.findTransactionsByAccountDestinationNumber(accountNumber);
        publisher.publishAccountTransactionsEvent(accountNumber, transactions);
        log.info("Retrieved {} transactions for account number {}", transactions.size(), accountNumber);
        return transactions;
    }

    @Override
    @Transactional
    public Transaction findTransactionById(Long transactionId) {
        Transaction transaction = repository.findById(transactionId).orElse(null);
        if (transaction == null) {
            // TODO return message to the web-service
            throw new EntityNotFoundException("Transaction with id " + transactionId + " not found");
        }
        publisher.publishTransactionDetailsEvent(transaction);
        log.info("Retrieved transaction with id: {}", transactionId);
        return transaction;
    }

    @Override
    @Transactional
    public void handleTransactionFailure(Long transactionId) {
        Transaction transaction = repository.findById(transactionId).orElse(null);
        if (transaction == null) {
            // TODO return message to the web-service
            throw new EntityNotFoundException("Transaction with id " + transactionId + " not found");
        }
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        repository.save(transaction);
        publisher.publishTransactionDetailsEvent(transaction);
        log.info("Failed transaction with id: {}", transactionId);
    }

    @Override
    @Transactional
    public void handleTransactionApproval(Long transactionId) {
        Transaction transaction = repository.findById(transactionId).orElse(null);
        if (transaction == null) {
            // TODO return message to the web-service
            throw new EntityNotFoundException("Transaction with id " + transactionId + " not found");
        }
        transaction.setTransactionStatus(TransactionStatus.APPROVED);
        repository.save(transaction);
        publisher.publishTransactionDetailsEvent(transaction);
        log.info("Approved transaction with id: {}", transactionId);
    }
}