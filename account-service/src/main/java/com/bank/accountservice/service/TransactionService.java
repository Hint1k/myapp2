package com.bank.accountservice.service;

import com.bank.accountservice.entity.Transaction;

import java.util.List;

public interface TransactionService {

    List<Transaction> findAll();
    Transaction saveTransaction(Transaction transaction);
}