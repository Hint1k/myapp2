package com.bank.accountservice.event.transaction;

import com.bank.accountservice.util.TransactionStatus;
import com.bank.accountservice.util.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionEvent {
    Long getTransactionId();

    BigDecimal getOldAmount();

    BigDecimal getAmount();

    LocalDateTime getTransactionTime();

    TransactionType getOldTransactionType();

    TransactionType getTransactionType();

    TransactionStatus getTransactionStatus();

    Long getAccountSourceNumber();

    Long getAccountDestinationNumber();
}