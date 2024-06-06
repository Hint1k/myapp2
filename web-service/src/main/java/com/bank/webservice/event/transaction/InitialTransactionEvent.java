package com.bank.webservice.event.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// TODO remove this class later, when the manual transaction process works
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitialTransactionEvent {
    private Long accountNumber;
    private BigDecimal balance;
}