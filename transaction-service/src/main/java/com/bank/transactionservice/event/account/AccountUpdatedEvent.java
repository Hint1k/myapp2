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
//public class AccountUpdatedEvent {
//
//    // TODO remove later the fields that cannot be updated
//    private Long accountId;  // this field can't be removed
//    private Long accountNumber;
//    private BigDecimal balance; // this field can be updated
//    private Currency currency; // this field can be updated
//    private AccountType accountType;
//    private AccountStatus accountStatus; // this field can be updated
//    private LocalDate openDate;
//    private List<Transaction> transactions; //this field can be updated
//    private Long customerId;
//}