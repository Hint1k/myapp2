package com.bank.webservice.event.account;

import com.bank.webservice.event.BaseEvent;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.AccountType;
import com.bank.webservice.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsEvent extends BaseEvent implements AccountEvent {

    private Long accountId;
    private Long accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private LocalDate openDate;
    private Long customerNumber;

    // no account id
    public AccountDetailsEvent(Long accountNumber, BigDecimal balance,
                               Currency currency, AccountType accountType,
                               AccountStatus accountStatus, LocalDate openDate,
                               Long customerNumber) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.openDate = openDate;
        this.customerNumber = customerNumber;
    }
}