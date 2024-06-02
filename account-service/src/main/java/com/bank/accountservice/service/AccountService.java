package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;

import java.util.List;

public interface AccountService {

    Account saveAccount(Account account);

    Account findAccountById(Long accountId);

    List<Account> findAllAccounts();

    void deleteAccountById(Long accountId);

    void updateAccountById(Account account);
}