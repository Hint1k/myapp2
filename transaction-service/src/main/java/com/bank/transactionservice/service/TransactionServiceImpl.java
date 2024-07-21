package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.publisher.TransactionEventPublisher;
import com.bank.transactionservice.repository.TransactionRepository;
import com.bank.transactionservice.util.TransactionStatus;
import com.bank.transactionservice.util.TransactionType;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
    public void updateTransaction(Transaction newTransaction) {
        Long transactionId = newTransaction.getTransactionId();
        Transaction oldTransaction = repository.findById(transactionId).orElse(null);
        if (oldTransaction == null) {
            // TODO return message to the web-service
            throw new EntityNotFoundException("Transaction with id " + transactionId + " not found");
        }
        BigDecimal oldAmount = oldTransaction.getAmount();
        TransactionType oldTransactionType = oldTransaction.getTransactionType();
        Long oldSourceAccountNumber = oldTransaction.getAccountSourceNumber();
        Long oldDestinationAccountNumber;
        if (Objects.equals(oldTransactionType, TransactionType.TRANSFER)) {
            oldDestinationAccountNumber = oldTransaction.getAccountDestinationNumber();
        } else {
            oldDestinationAccountNumber = oldSourceAccountNumber;
        }
        repository.save(newTransaction); // JPA repository should merge instead of save
        publisher.publishTransactionUpdatedEvent(newTransaction, oldAmount, oldTransactionType,
                oldSourceAccountNumber, oldDestinationAccountNumber);
        log.info("Transaction with id: {} updated", transactionId);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId) {
        Transaction transaction = repository.findById(transactionId).orElse(null);
        if (transaction == null) {
            // TODO return message to the web-service
            throw new EntityNotFoundException("Transaction with id " + transactionId + " not found");
        }
        repository.deleteById(transactionId);
        publisher.publishTransactionDeletedEvent(transaction);
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

    @Override
    @Transactional
    public void freezeTransactions(Long accountNumber) {
        List<Transaction> transactions = repository.findTransactionsByAccountNumber(accountNumber);
        for (Transaction transaction : transactions) {
            transaction.setTransactionStatus(TransactionStatus.FROZEN);
            /* changing the account number of the deleted account to zero in all transactions
            to avoid confusions when later a new account with the same number is created */
            if (transaction.getAccountSourceNumber().equals(accountNumber)) {
                transaction.setAccountSourceNumber(0L);
            }
            if (transaction.getAccountDestinationNumber().equals(accountNumber)) {
                transaction.setAccountDestinationNumber(0L);
            }
            repository.save(transaction);
        }
        log.info("Froze {} transactions", transactions.size());
        publisher.publishAllTransactionsEvent(transactions);
    }

    @Override
    @Transactional
    public void suspendOrUnsuspendTransactions(Long accountNumber, String suspend) {
        List<Transaction> transactions = repository.findTransactionsByAccountNumber(accountNumber);
        for (Transaction transaction : transactions) {
            if (suspend.equalsIgnoreCase("suspend")) {
                transaction.setTransactionStatus(TransactionStatus.SUSPENDED);
            } else {
                transaction.setTransactionStatus(TransactionStatus.APPROVED);
            }
            repository.save(transaction);
        }
        log.info("Suspended {} transactions", transactions.size());
        publisher.publishAllTransactionsEvent(transactions);
    }
}