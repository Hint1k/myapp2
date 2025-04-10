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
public class TransactionUpdatedEvent {

    private Long transactionId; // this field can't be removed
    private BigDecimal oldAmount;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType oldTransactionType;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long oldAccountSourceNumber;
    private Long accountSourceNumber;
    private Long oldAccountDestinationNumber;
    private Long accountDestinationNumber;
}