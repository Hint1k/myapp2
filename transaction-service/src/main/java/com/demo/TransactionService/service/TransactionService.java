package com.demo.TransactionService.service;

import com.demo.TransactionService.dto.AccountCreatedEventDTO;
import com.demo.TransactionService.dto.AccountDTO;

public interface TransactionService {

   AccountDTO createInitialTransaction(AccountCreatedEventDTO event);
}