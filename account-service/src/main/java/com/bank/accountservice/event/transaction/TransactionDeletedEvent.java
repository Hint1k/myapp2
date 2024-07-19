package com.bank.accountservice.event.transaction;

import com.bank.accountservice.util.TransactionStatus;
import com.bank.accountservice.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDeletedEvent {
    private Long transactionId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private Long accountSourceNumber;
    private Long accountDestinationNumber;

    // no transaction id
    public TransactionDeletedEvent(BigDecimal amount, TransactionType transactionType, TransactionStatus status,
                                   Long accountSourceNumber, Long accountDestinationNumber) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.status = status;
        this.accountSourceNumber = accountSourceNumber;
        this.accountDestinationNumber = accountDestinationNumber;
    }
}