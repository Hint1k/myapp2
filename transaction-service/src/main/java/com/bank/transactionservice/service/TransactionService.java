package com.bank.transactionservice.service;

import com.bank.transactionservice.dto.AccountCreatedEvent;
import com.bank.transactionservice.dto.Account;

public interface TransactionService {

   Account createInitialTransaction(AccountCreatedEvent event);
}