package com.demo.myapp2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;

    // bidirectional relationship, owning side
    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}