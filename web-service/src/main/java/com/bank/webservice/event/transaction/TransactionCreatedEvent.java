package com.bank.webservice.event.transaction;

import com.bank.webservice.dto.Account;
import com.bank.webservice.util.TransactionType;
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
    private Long accountNumber;

    public TransactionCreatedEvent(BigDecimal amount, LocalDateTime transactionTime,
                                   TransactionType transactionType, Long accountNumber) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.accountNumber = accountNumber;
    }
}