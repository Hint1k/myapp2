package com.bank.webservice.listener;

import com.bank.webservice.cache.AccountCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionEventListener {

    private final AccountCache cache;

    @Autowired
    public TransactionEventListener(AccountCache cache) {
        this.cache = cache;
    }


}