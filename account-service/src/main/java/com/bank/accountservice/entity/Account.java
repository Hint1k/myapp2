package com.bank.accountservice.entity;

import com.bank.accountservice.util.Currency;
import com.bank.accountservice.util.AccountStatus;
import com.bank.accountservice.util.AccountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "account")
@Data
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

    /// bidirectional relationship, referencing side
    @OneToMany(mappedBy = "account",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<TransactionHistory> transactionHistories;

    @Column(name = "customer_id")
    private Long customerId;

    // no accountId
    public Account(Long accountNumber, BigDecimal balance,
                   Currency currency, AccountType accountType,
                   AccountStatus accountStatus, LocalDate openDate,
                   List<TransactionHistory> transactionHistories,
                   Long customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.openDate = openDate;
        this.transactionHistories = transactionHistories;
        this.customerId = customerId;
    }

    // no accountId and no transactionHistories
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

    public Account(Long accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
}