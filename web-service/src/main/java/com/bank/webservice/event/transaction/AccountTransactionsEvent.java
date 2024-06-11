package com.bank.webservice.event.transaction;

import com.bank.webservice.dto.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountTransactionsEvent {
    private Long accountNumber;
    private List<Transaction> transactions;
}