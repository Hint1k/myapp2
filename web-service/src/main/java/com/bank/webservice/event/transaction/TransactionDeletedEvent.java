package com.bank.webservice.event.transaction;

import com.bank.webservice.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDeletedEvent extends BaseEvent {
    private Long transactionId;
}