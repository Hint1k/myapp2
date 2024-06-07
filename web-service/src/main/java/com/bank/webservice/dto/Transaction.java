package com.bank.webservice.dto;

import com.bank.webservice.util.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.lang.Integer.MAX_VALUE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private Long transactionId;

    @NotNull(message = "Balance is required")
    @Min(value = 1) // TODO create later custom validation for minimum value = 0.01
    @Digits(integer = MAX_VALUE, fraction = 2)
    private BigDecimal amount;

    @NotNull(message = "Transaction time is required")
    private LocalDateTime transactionTime;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Account Id is required")
    // TODO add custom annotation for account id later
    private Account account;

    {
        // sets transaction time = current time
        this.transactionTime = LocalDateTime.now();
    }

    public Transaction(BigDecimal amount, LocalDateTime transactionTime,
                       TransactionType transactionType, Account account) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.account = account;
    }
}