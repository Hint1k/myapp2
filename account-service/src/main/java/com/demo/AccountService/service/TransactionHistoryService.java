package com.demo.AccountService.service;

import com.demo.AccountService.entity.TransactionHistory;

import java.util.List;

public interface TransactionHistoryService {

    List<TransactionHistory> findAll();
}