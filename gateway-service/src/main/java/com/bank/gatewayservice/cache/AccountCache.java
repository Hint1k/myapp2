package com.bank.gatewayservice.cache;

import com.bank.gatewayservice.dto.Account;

public interface AccountCache {

    Account getAccountFromCache(Long accountId);
}