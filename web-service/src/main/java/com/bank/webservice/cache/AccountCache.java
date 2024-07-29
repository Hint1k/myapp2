package com.bank.webservice.cache;

import com.bank.webservice.dto.Account;

import java.util.List;

public interface AccountCache {

    void addAccountToCache(Long accountId, Account account);

    void addAllAccountsToCache(List<Account> accounts);

    void updateAccountInCache(Long accountId, Account account);

    void deleteAccountFromCache(Long accountId);

    List<Account> getAllAccountsFromCache();

    Account getAccountFromCache(Long accountId);

    Account getAccountFromCacheByAccountNumber(Long accountNumber);

    List<Account> getAccountsFromCacheByCustomerNumber(Long customerNumber);
}