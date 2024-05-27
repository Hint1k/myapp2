package com.bank.transactionservice.service;

import com.bank.transactionservice.dto.AccountCreatedEventDTO;
import com.bank.transactionservice.dto.AccountDTO;

public interface TransactionService {

   AccountDTO createInitialTransaction(AccountCreatedEventDTO event);
}