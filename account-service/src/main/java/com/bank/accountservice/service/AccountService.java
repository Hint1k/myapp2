package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;

import java.util.List;

public interface AccountService {

    void saveAccount(Account account);

    void updateAccount(Account account);

    void deleteAccount(Long accountId);

    List<Account> findAllAccounts();

    Account findAccountById(Long accountId);

    Account findAccountByItsNumber(Long accountNumber);
}