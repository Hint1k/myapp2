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
public class TransactionUpdatedEvent extends BaseEvent implements TransactionEvent {

    private Long transactionId; // this field can't be removed
    private BigDecimal oldAmount;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private TransactionType oldTransactionType;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Long oldAccountSourceNumber;
    private Long accountSourceNumber;
    private Long oldAccountDestinationNumber;
    private Long accountDestinationNumber;

    // no old amount, no old transaction type, no old account number fields
    public TransactionUpdatedEvent(Long transactionId, BigDecimal amount, LocalDateTime transactionTime,
                                   TransactionType transactionType, TransactionStatus transactionStatus,
                                   Long accountSourceNumber, Long accountDestinationNumber) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.accountSourceNumber = accountSourceNumber;
        this.accountDestinationNumber = accountDestinationNumber;
    }
}