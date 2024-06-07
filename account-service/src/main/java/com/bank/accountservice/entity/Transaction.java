package com.bank.accountservice.entity;

import com.bank.accountservice.util.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long transactionId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    @Column(name ="transaction_type", nullable = false)
    private TransactionType transactionType;

    // bidirectional relationship, owning side
    @ManyToOne(cascade = {
            CascadeType.DETACH,
            CascadeType.MERGE,
//            CascadeType.PERSIST, // does not save new transactions if account already in db
            CascadeType.REFRESH})
    @JoinColumn(name = "account_number", nullable = false)
    private Account account;

    public Transaction(BigDecimal amount, LocalDateTime transactionTime,
                       TransactionType transactionType, Account account) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.account = account;
    }
}