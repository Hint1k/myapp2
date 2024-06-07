package com.bank.accountservice.event.account;

import com.bank.accountservice.entity.Transaction;
import com.bank.accountservice.util.AccountStatus;
import com.bank.accountservice.util.AccountType;
import com.bank.accountservice.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreatedEvent {
    private Long accountId;
    private Long accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private LocalDate openDate;
    private List<Transaction> transactions;
    private Long customerId;

    // no id
    public AccountCreatedEvent(Long accountNumber, BigDecimal balance,
                               Currency currency, AccountType accountType,
                               AccountStatus accountStatus, List<Transaction> transactions,
                               LocalDate openDate, Long customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.transactions = transactions;
        this.openDate = openDate;
        this.customerId = customerId;
    }
}