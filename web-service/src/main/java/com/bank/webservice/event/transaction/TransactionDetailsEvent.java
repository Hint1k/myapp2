package com.bank.webservice.event.transaction;

import com.bank.webservice.event.BaseEvent;
import com.bank.webservice.util.TransactionStatus;
import com.bank.webservice.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetailsEvent extends BaseEvent implements TransactionEvent {

    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long accountSourceNumber;
    private Long accountDestinationNumber;
}