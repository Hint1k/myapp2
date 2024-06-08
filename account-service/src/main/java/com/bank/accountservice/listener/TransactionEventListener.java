//package com.bank.accountservice.listener;
//
//import com.bank.accountservice.entity.Transaction;
//import com.bank.accountservice.event.transaction.TransactionCreatedEvent;
//import com.bank.accountservice.service.AccountService;
//import com.bank.accountservice.service.TransactionService;
//import com.bank.accountservice.entity.Account;
//import com.bank.accountservice.util.TransactionType;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Component
//@Slf4j
//public class TransactionEventListener {
//
//    private final TransactionService transactionService;
//
//    private final AccountService accountService;
//
//    @Autowired
//    public TransactionEventListener(TransactionService transactionService, AccountService accountService) {
//        this.transactionService = transactionService;
//        this.accountService = accountService;
//    }
//
//    @KafkaListener(topics = "transaction-creation-completed", groupId = "account-service")
//    public void handleTransactionCreatedEvent(TransactionCreatedEvent event, Acknowledgment acknowledgment) {
//        log.info("Received transaction creation completed event for account number: {}",
//                event.getAccount().getAccountNumber());
//        try {
//            Transaction transaction = new Transaction(
//                    event.getAmount(),
//                    event.getTransactionTime(),
//                    event.getTransactionType(),
//                    event.getAccount()
//            );
//
////            Account account = transaction.getAccount();
////            BigDecimal currentBalance = account.getBalance();
////            BigDecimal newBalance = event.getAmount();
//            Long accountId = event.getAccount().getAccountNumber();
//            Account account = accountService.findAccountById(accountId);
//            BigDecimal oldBalance = account.getBalance();
//            BigDecimal newBalance = transaction.getAmount();
//
//            if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
//                account.setBalance(oldBalance.add(newBalance));
//            }
//            accountService.saveAccount(account);
//
//            transactionService.saveTransaction(transaction);
//
//            log.info("Completed transaction for account number: {}", account.getAccountNumber());
//            acknowledgment.acknowledge();
//        } catch (Exception e) {
//            log.error("Error saving transaction: {}", e.getMessage());
//            // TODO implement error handling later
//        }
//    }
//}