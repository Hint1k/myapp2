//package com.bank.accountservice.event.transaction;
//
//import com.bank.accountservice.entity.Account;
//import com.bank.accountservice.util.TransactionType;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class TransactionCreatedEvent {
//    private Long transactionId;
//    private BigDecimal amount;
//    private LocalDateTime transactionTime;
//    private TransactionType transactionType;
//    private Account account;
//
//    public TransactionCreatedEvent(BigDecimal amount, LocalDateTime transactionTime,
//                                   TransactionType transactionType, Account account) {
//        this.amount = amount;
//        this.transactionTime = transactionTime;
//        this.transactionType = transactionType;
//        this.account = account;
//    }
//}