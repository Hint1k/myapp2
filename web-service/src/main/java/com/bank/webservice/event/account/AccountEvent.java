package com.bank.webservice.event.account;

import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.AccountType;
import com.bank.webservice.util.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AccountEvent {

    Long getAccountId();

    Long getAccountNumber();

    BigDecimal getBalance();

    Currency getCurrency();

    AccountType getAccountType();

    AccountStatus getAccountStatus();

    LocalDate getOpenDate();

    Long getCustomerNumber();
}