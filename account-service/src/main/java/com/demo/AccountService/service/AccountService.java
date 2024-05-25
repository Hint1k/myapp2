package com.demo.AccountService.service;

import com.demo.AccountService.entity.Account;

public interface AccountService {

    Account saveAccount(Account account);
    Account findAccountById(Long id);
}