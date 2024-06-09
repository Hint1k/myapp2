package com.bank.webservice.event.combined;

import com.bank.webservice.dto.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountTransactionEvent {
    private Long accountId;
    private List<Transaction> transactions;
}