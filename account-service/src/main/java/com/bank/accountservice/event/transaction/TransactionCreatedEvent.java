package com.bank.accountservice.event.transaction;

import com.bank.accountservice.util.TransactionStatus;
import com.bank.accountservice.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent implements TransactionEvent {
    // TODO combine later with classes TransactionDetailsEvent and TransactionUpdatedEvent
    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long accountSourceNumber;
    private Long accountDestinationNumber;

    // no transaction id
    public TransactionCreatedEvent(BigDecimal amount, LocalDateTime transactionTime,
                                   TransactionType transactionType, TransactionStatus transactionStatus,
                                   Long accountSourceNumber, Long accountDestinationNumber) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.accountSourceNumber = accountSourceNumber;
        this.accountDestinationNumber = accountDestinationNumber;
    }

    @Override
    public BigDecimal getOldAmount() {
        return null;
    }

    @Override
    public TransactionType getOldTransactionType() {
        return null;
    }

    @Override
    public Long getOldAccountSourceNumber() {
        return 0L;
    }

    @Override
    public Long getOldAccountDestinationNumber() {
        return 0L;
    }
}