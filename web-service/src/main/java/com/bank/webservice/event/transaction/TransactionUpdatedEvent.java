package com.bank.webservice.event.transaction;

import com.bank.webservice.util.TransactionStatus;
import com.bank.webservice.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUpdatedEvent {
    // TODO combine later with classes TransactionCreatedEvent and TransactionDetailsEvent
    private Long transactionId; // this field can't be removed
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long accountSourceNumber;
    private Long accountDestinationNumber;
}