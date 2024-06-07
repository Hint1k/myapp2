package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.publisher.TransactionEventPublisher;
import com.bank.transactionservice.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Transaction saveTransaction(Transaction transaction) {
        Transaction savedTransaction = repository.save(transaction);
        publisher.publishTransactionCreatedEvent(savedTransaction);
        log.info("Transaction saved: {}", savedTransaction);
        return savedTransaction;
    }
}