package com.bank.accountservice.service;

import com.bank.accountservice.entity.Transaction;
import com.bank.accountservice.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public List<Transaction> findAll() {
        List<Transaction> transactions = repository.findAll();
        return transactions;
    }

    @Override
    @Transactional
    public Transaction saveTransaction(Transaction transaction) {
        Transaction savedTransaction = repository.save(transaction);
        log.info("Transaction saved: {}", savedTransaction);
        return savedTransaction;
    }
}