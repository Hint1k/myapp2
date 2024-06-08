package com.bank.transactionservice.entity;

import com.bank.transactionservice.util.AccountStatus;
import com.bank.transactionservice.util.AccountType;
import com.bank.transactionservice.util.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    // TODO move this class to DTO package later
    private Long accountId;
    private Long accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private LocalDate openDate;
    private Long customerId;

    // no account id
    public Account(Long accountNumber, BigDecimal balance,
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