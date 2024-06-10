package com.bank.transactionservice.event;

import com.bank.transactionservice.entity.Transaction;
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