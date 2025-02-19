package com.bank.webservice.event.transaction;

import com.bank.webservice.util.TransactionStatus;
import com.bank.webservice.util.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionEvent {

    Long getTransactionId();

    BigDecimal getAmount();

    LocalDateTime getTransactionTime();

    TransactionType getTransactionType();

    TransactionStatus getTransactionStatus();

    Long getAccountSourceNumber();

    Long getAccountDestinationNumber();
}