package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Account;
import com.bank.transactionservice.event.AccountCreatedEvent;

public interface TransactionService {

   Account createInitialTransaction(AccountCreatedEvent event);
}