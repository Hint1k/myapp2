package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Account;

public interface AccountService {

    Account findAccountById(Long accountId);

    Account saveAccount(Account account);

    void deleteAccountById(Long accountId);

    void updateAccount(Account account);
}