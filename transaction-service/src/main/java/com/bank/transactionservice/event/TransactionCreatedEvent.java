package com.bank.transactionservice.event;

import com.bank.transactionservice.util.TransactionStatus;
import com.bank.transactionservice.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    // TODO combine later with classes TransactionDetailsEvent and TransactionUpdatedEvent
    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long accountDestinationNumber;

    // no transaction id
    public TransactionCreatedEvent(BigDecimal amount, LocalDateTime transactionTime,
                                   TransactionType transactionType, TransactionStatus transactionStatus,
                                   Long accountDestinationNumber) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.accountDestinationNumber = accountDestinationNumber;
    }
}