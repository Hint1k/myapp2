package com.demo.AccountService.entity;

import com.demo.AccountService.util.Currency;
import com.demo.AccountService.util.AccountStatus;
import com.demo.AccountService.util.AccountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "account")
@Data // getters, setters, hashcode, equals, toString
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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

    @Column(name = "user_id")
    private Long userId;

    public Account(Long accountNumber, BigDecimal balance,
                   Currency currency, AccountType accountType,
                   AccountStatus accountStatus, LocalDate openDate,
                   List<TransactionHistory> transactionHistories,
                   Long userId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.openDate = openDate;
        this.transactionHistories = transactionHistories;
        this.userId = userId;
    }
}