package com.bank.accountservice.event.transaction;

import com.bank.accountservice.util.TransactionStatus;
import com.bank.accountservice.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUpdatedEvent implements TransactionEvent {
    // TODO combine later with classes TransactionCreatedEvent
    private Long transactionId; // this field can't be removed
    private BigDecimal oldAmount;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType oldTransactionType;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long accountSourceNumber;
    private Long accountDestinationNumber;

    // no oldAmount and no oldTransactionType fields
    public TransactionUpdatedEvent(Long transactionId, BigDecimal amount, LocalDateTime transactionTime,
                                   TransactionType transactionType, TransactionStatus transactionStatus,
                                   Long accountSourceNumber, Long accountDestinationNumber) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.accountSourceNumber = accountSourceNumber;
        this.accountDestinationNumber = accountDestinationNumber;
    }
}