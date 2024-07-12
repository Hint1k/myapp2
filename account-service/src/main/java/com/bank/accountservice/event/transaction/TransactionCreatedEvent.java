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
    private BigDecimal oldAmount;
    private BigDecimal newAmount;
    private LocalDateTime transactionTime;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long accountSourceNumber;
    private Long accountDestinationNumber;

    // no transaction id
    public TransactionCreatedEvent(BigDecimal oldAmount, BigDecimal newAmount, LocalDateTime transactionTime,
                                   TransactionType transactionType, TransactionStatus transactionStatus,
                                   Long accountSourceNumber, Long accountDestinationNumber) {
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.accountSourceNumber = accountSourceNumber;
        this.accountDestinationNumber = accountDestinationNumber;
    }

    @Override
    public BigDecimal getAmount() {
        return null;
    }

    @Override
    public TransactionType getOldTransactionType() {
        return null;
    }
}