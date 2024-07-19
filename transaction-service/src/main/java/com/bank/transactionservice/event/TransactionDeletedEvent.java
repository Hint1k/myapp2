package com.bank.transactionservice.event;

import com.bank.transactionservice.util.TransactionType;
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
    private Long accountSourceNumber;
    private Long accountDestinationNumber;

    // no transaction id
    public TransactionDeletedEvent(BigDecimal amount, TransactionType transactionType,
                                   Long accountSourceNumber, Long accountDestinationNumber) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.accountSourceNumber = accountSourceNumber;
        this.accountDestinationNumber = accountDestinationNumber;
    }
}