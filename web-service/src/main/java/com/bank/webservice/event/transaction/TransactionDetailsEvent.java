package com.bank.webservice.event.transaction;

import com.bank.webservice.dto.Account;
import com.bank.webservice.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetailsEvent {
    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType transactionType;
    private Account account;

    // no Id
    public TransactionDetailsEvent(BigDecimal amount, LocalDateTime transactionTime,
                                   TransactionType transactionType, Account account) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.account = account;
    }
}