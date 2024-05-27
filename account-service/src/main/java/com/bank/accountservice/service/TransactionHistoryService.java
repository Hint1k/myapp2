package com.bank.accountservice.service;

import com.bank.accountservice.entity.TransactionHistory;

import java.util.List;

public interface TransactionHistoryService {

    List<TransactionHistory> findAll();
}