package com.bank.webservice.event.transaction;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllTransactionsEvent extends BaseEvent {
    private List<Transaction> transactions;
}