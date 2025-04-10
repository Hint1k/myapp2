package com.bank.transactionservice.event.transaction;

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
}