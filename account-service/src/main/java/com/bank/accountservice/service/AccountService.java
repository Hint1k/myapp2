package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;

public interface AccountService {

    Account saveAccount(Account account);
    Account findAccountById(Long id);
}