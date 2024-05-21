package com.demo.TransactionService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data // getters, setters, hashcode, equals, toString
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private Long accountId;
}