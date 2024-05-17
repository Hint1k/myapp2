package com.demo.myapp2.entity;

import com.demo.myapp2.util.AccountStatus;
import com.demo.myapp2.util.AccountType;
import com.demo.myapp2.util.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "currency")
    private Currency currency;

    @Column(name = "account_type")
    private AccountType accountType;

    @Column(name = "status")
    private AccountStatus status;

    @Column(name = "open_date")
    private LocalDate openDate;

    /// bidirectional relationship, referencing side
    @OneToMany(mappedBy = "account",
            targetEntity = Transaction.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    @Column(name = "user_id")
    private Long userId;
}