package com.bank.webservice.event.transaction;

import com.bank.webservice.util.TransactionType;
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
    private Long accountDestinationNumber;

    // no transaction id
    public TransactionDetailsEvent(BigDecimal amount, LocalDateTime transactionTime,
                                   TransactionType transactionType, Long accountDestinationNumber) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.accountDestinationNumber = accountDestinationNumber;
    }
}