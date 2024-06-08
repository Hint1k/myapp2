package com.bank.accountservice.entity;

import com.bank.accountservice.util.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    // TODO move this class to DTO package later
    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType transactionType;
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