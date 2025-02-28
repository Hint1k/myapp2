package com.bank.accountservice.data;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.util.AccountStatus;
import com.bank.accountservice.util.AccountType;
import com.bank.accountservice.util.Currency;
import com.bank.accountservice.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TestData {

    public Long oldAccountSourceNumber;
    public Long newAccountSourceNumber;
    public Long oldAccountDestinationNumber;
    public Long newAccountDestinationNumber;
    public BigDecimal oldAmount;
    public BigDecimal newAmount;
    public TransactionType oldTransactionType;
    public TransactionType newTransactionType;
    public Long transactionId;
    public Account newSourceAccount;
    public Account oldSourceAccount;
    public Account newDestinationAccount;
    public Account oldDestinationAccount;

    public TestData() {
        this.oldAccountSourceNumber = 1L;
        this.newAccountSourceNumber = 2L;
        this.oldAccountDestinationNumber = 3L;
        this.newAccountDestinationNumber = 4L;
        this.oldAmount = BigDecimal.valueOf(100);
        this.newAmount = BigDecimal.valueOf(200);
        this.oldTransactionType = TransactionType.TRANSFER;
        this.newTransactionType = TransactionType.TRANSFER;
        this.transactionId = 10L;
        this.newSourceAccount = new Account(newAccountSourceNumber, newAmount, Currency.USD, AccountType.SAVINGS,
                AccountStatus.ACTIVE, LocalDate.now(), 121L);
        this.oldSourceAccount = new Account(newAccountSourceNumber, newAmount, Currency.USD, AccountType.SAVINGS,
                AccountStatus.ACTIVE, LocalDate.now(), 122L);
        this.newDestinationAccount = new Account(newAccountDestinationNumber, newAmount, Currency.USD,
                AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), 123L);
        this.oldDestinationAccount = new Account(newAccountDestinationNumber, newAmount, Currency.USD,
                AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), 124L);
    }
}