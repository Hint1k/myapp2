package com.bank.transactionservice.event.transaction;

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

    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long accountSourceNumber;
    private Long accountDestinationNumber;
}