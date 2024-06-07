package com.bank.webservice.converter;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToAccount implements Converter<String, Account> {

    private final AccountCache cache;

    @Autowired
    public StringToAccount(AccountCache cache) {
        this.cache = cache;
    }

    @Override
    public Account convert(String source) {
        if (source.isEmpty()) {
            return null;
        } else {
            Long accountId = Long.parseLong(source);
            Account account = cache.getFromCacheById(accountId);
            return account;
        }
    }
}