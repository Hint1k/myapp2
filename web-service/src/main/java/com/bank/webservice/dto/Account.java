package com.bank.webservice.dto;

import com.bank.webservice.util.Currency;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {
    private Long accountId;
    private Long accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private LocalDate openDate;
    private Long customerId;

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