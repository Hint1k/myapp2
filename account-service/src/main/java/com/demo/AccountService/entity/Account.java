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
    private String accountNumber;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false)
    private Currency currency;

    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    /// bidirectional relationship, referencing side
    @OneToMany(mappedBy = "account",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<TransactionHistory> transactionHistories;

    @Column(name = "user_id")
    private Long userId;
}