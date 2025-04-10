package com.bank.accountservice.entity;

import com.bank.accountservice.util.Currency;
import com.bank.accountservice.util.AccountStatus;
import com.bank.accountservice.util.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "account")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long accountId;

    @Column(name = "account_number", nullable = false)
    private Long accountNumber;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    @Column(name = "customer_number")
    private Long customerNumber;

    // no account id
    public Account(Long accountNumber, BigDecimal balance,
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountId, account.accountId) && Objects.equals(accountNumber, account.accountNumber)
                && Objects.equals(balance, account.balance) && currency == account.currency
                && accountType == account.accountType && accountStatus == account.accountStatus
                && Objects.equals(openDate, account.openDate)
                && Objects.equals(customerNumber, account.customerNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                accountId, accountNumber, balance, currency, accountType, accountStatus, openDate, customerNumber
        );
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", accountNumber=" + accountNumber +
                ", balance=" + balance +
                ", currency=" + currency +
                ", accountType=" + accountType +
                ", accountStatus=" + accountStatus +
                ", openDate=" + openDate +
                ", customerNumber=" + customerNumber +
                '}';
    }
}