package com.bank.transactionservice.entity;

import com.bank.transactionservice.util.TransactionStatus;
import com.bank.transactionservice.util.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transaction")
@Setter
@Getter
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

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @Column(name = "account_source", nullable = false)
    private Long accountSourceNumber;

    @Column(name = "account_destination", nullable = false)
    private Long accountDestinationNumber;

    // no transaction id
    public Transaction(BigDecimal amount, LocalDateTime transactionTime,
                       TransactionType transactionType, TransactionStatus transactionStatus,
                       Long accountDestinationNumber, Long accountSourceNumber) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.accountDestinationNumber = accountDestinationNumber;
        this.accountSourceNumber = accountSourceNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId) && Objects.equals(amount, that.amount)
                && Objects.equals(transactionTime, that.transactionTime) && transactionType == that.transactionType
                && transactionStatus == that.transactionStatus
                && Objects.equals(accountSourceNumber, that.accountSourceNumber)
                && Objects.equals(accountDestinationNumber, that.accountDestinationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, amount, transactionTime, transactionType, transactionStatus,
                accountSourceNumber, accountDestinationNumber);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", amount=" + amount +
                ", transactionTime=" + transactionTime +
                ", transactionType=" + transactionType +
                ", transactionStatus=" + transactionStatus +
                ", accountSourceNumber=" + accountSourceNumber +
                ", accountDestinationNumber=" + accountDestinationNumber +
                '}';
    }
}