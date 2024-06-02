package com.bank.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // getters, setters, hashcode, equals, toString
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    private BigDecimal amount;
    private Long accountId;
}