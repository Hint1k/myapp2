package com.bank.webservice.dto;

import com.bank.webservice.util.TransactionStatus;
import com.bank.webservice.util.TransactionType;
import com.bank.webservice.validation.MinimumAmount;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.lang.Integer.MAX_VALUE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Serializable {

    private Long transactionId;

    @NotNull(message = "Balance is required")
    @MinimumAmount // set to 0.01, custom annotation in validation package
    @Digits(integer = MAX_VALUE, fraction = 2, message = "Only 2 decimal places are allowed")
    private BigDecimal amount;

    @NotNull(message = "Transaction time is required")
    private LocalDateTime transactionTime;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Transaction status is required")
    private TransactionStatus transactionStatus;

    // Custom validation logic is in TransactionController class
    private Long accountSourceNumber;

    // Custom validation logic is in TransactionController class
    private Long accountDestinationNumber;

    { // TODO change to ZonedDateTime later and change init2.sql
        // sets transaction time = current time
        this.transactionTime = LocalDateTime.now();
        // sets default transaction type
        this.transactionStatus = TransactionStatus.PENDING;
    }

    // no transaction id
    public Transaction(BigDecimal amount, LocalDateTime transactionTime,
                       TransactionType transactionType, TransactionStatus transactionStatus,
                       Long accountDestinationNumber, Long accountSourceNumber) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.accountDestinationNumber = accountDestinationNumber;
        this.accountSourceNumber = accountSourceNumber;
    }
}