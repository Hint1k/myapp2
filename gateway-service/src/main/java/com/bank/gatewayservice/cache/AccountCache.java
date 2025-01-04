package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Account;

import java.util.List;

public interface AccountCache {

    List<Account> getAllAccountsFromCache();

    Account getAccountFromCache(Long accountId);

    Account getAccountFromCacheByAccountNumber(Long accountNumber);

    List<Account> getAccountsFromCacheByCustomerNumber(Long customerNumber);

    List<Long> getAccountNumbersFromCacheByCustomerNumber(Long customerNumber);
}