package com.bank.transactionservice.event.transaction;

import com.bank.transactionservice.util.TransactionStatus;
import com.bank.transactionservice.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetailsEvent {
    // TODO combine later with classes TransactionCreatedEvent and TransactionUpdatedEvent
    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long accountSourceNumber;
    private Long accountDestinationNumber;

    // no transaction id
    public TransactionDetailsEvent(BigDecimal amount, LocalDateTime transactionTime,
                                   TransactionType transactionType, TransactionStatus transactionStatus,
                                   Long accountDestinationNumber, Long accountSourceNumber) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.accountDestinationNumber = accountDestinationNumber;
        this.accountSourceNumber = accountSourceNumber;
    }
}