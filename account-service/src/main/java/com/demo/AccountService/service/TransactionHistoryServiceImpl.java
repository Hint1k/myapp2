package com.demo.AccountService.service;

import com.demo.AccountService.entity.TransactionHistory;
import com.demo.AccountService.repository.TransactionHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final TransactionHistoryRepository repository;

    @Autowired
    public TransactionHistoryServiceImpl(TransactionHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public List<TransactionHistory> findAll() {
        List<TransactionHistory> transactionHistories = repository.findAll();
        return transactionHistories;
    }
}