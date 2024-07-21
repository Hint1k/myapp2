package com.bank.transactionservice.event.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionApprovedEvent {
    // TODO combine later with TransactionFailedEvent
    private Long transactionId;
}