package com.bank.accountservice.event.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFailedEvent {
    // TODO combine later with TransactionApprovedEvent
  private Long transactionId;
}