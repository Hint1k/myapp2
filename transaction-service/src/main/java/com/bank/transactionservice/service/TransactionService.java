package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Transaction;

public interface TransactionService {

   Transaction saveTransaction(Transaction transaction);
}