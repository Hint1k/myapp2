package com.bank.transactionservice.event.account;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.util.AccountStatus;
import com.bank.transactionservice.util.AccountType;
import com.bank.transactionservice.util.Currency;
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
    private List<Transaction> transactions;
    private LocalDate openDate;
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