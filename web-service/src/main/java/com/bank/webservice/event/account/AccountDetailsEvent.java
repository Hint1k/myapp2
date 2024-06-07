package com.bank.webservice.event.account;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.AccountType;
import com.bank.webservice.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private List<Transaction> transactions;
    private Long customerId;

    // no id
    public AccountDetailsEvent(Long accountNumber, BigDecimal balance,
                               Currency currency, AccountType accountType,
                               AccountStatus accountStatus, LocalDate openDate,
                               List<Transaction> transactions, Long customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.openDate = openDate;
        this.transactions = transactions;
        this.customerId = customerId;
    }
}