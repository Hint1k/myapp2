package com.bank.transactionservice.entity;

import com.bank.transactionservice.util.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "account_destination", nullable = false)
    private Long accountDestinationNumber;

    // no transaction id
    public Transaction(BigDecimal amount, LocalDateTime transactionTime,
                       TransactionType transactionType, Long accountDestinationNumber) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.accountDestinationNumber = accountDestinationNumber;
    }
}