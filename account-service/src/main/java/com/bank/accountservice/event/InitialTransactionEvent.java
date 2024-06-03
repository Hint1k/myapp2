package com.bank.accountservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // getters, setters, hashcode, equals, toString
@NoArgsConstructor
@AllArgsConstructor
public class InitialTransactionEvent {
    private Long accountNumber;
    private BigDecimal balance;
}