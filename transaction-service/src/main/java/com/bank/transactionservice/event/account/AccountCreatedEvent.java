//package com.bank.transactionservice.event.account;
//
//import com.bank.transactionservice.entity.Transaction;
//import com.bank.transactionservice.util.AccountStatus;
//import com.bank.transactionservice.util.AccountType;
//import com.bank.transactionservice.util.Currency;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class AccountCreatedEvent {
//    // TODO combine later with AccountDetailsEvent class
//    private Long accountId;
//    private Long accountNumber;
//    private BigDecimal balance;
//    private Currency currency;
//    private AccountType accountType;
//    private AccountStatus accountStatus;
//    private LocalDate openDate;
//    private List<Transaction> transactions;
//    private Long customerId;
//
//    // no account id
//    public AccountCreatedEvent(Long accountNumber, BigDecimal balance,
//                               Currency currency, AccountType accountType,
//                               AccountStatus accountStatus, LocalDate openDate,
//                               List<Transaction> transactions, Long customerId) {
//        this.accountNumber = accountNumber;
//        this.balance = balance;
//        this.currency = currency;
//        this.accountType = accountType;
//        this.accountStatus = accountStatus;
//        this.openDate = openDate;
//        this.transactions = transactions;
//        this.customerId = customerId;
//    }
//}