package com.bank.accountservice.event.account;

import com.bank.accountservice.util.AccountStatus;
import com.bank.accountservice.util.AccountType;
import com.bank.accountservice.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsEvent {
    private Long accountId;
    private Long accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private LocalDate openDate;
    private Long customerId;

    // no account id
    public AccountDetailsEvent(Long accountNumber, BigDecimal balance,
                               Currency currency, AccountType accountType,
                               AccountStatus accountStatus, LocalDate openDate,
                               Long customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.openDate = openDate;
        this.customerId = customerId;
    }
}