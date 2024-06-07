package com.bank.transactionservice.listener;

import com.bank.transactionservice.entity.Account;
import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.event.transaction.TransactionCreatedEvent;
import com.bank.transactionservice.service.AccountService;
import com.bank.transactionservice.service.TransactionService;
import com.bank.transactionservice.util.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class TransactionEventListener {

    private final TransactionService transactionService;

    private final AccountService accountService;

    @Autowired
    public TransactionEventListener(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @KafkaListener(topics = "transaction-creation-requested", groupId = "transaction-service")
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received transaction-creation-requested event for account number: {}",
                event.getAccount().getAccountNumber());
        try {
            Transaction transaction = new Transaction(
                    event.getAmount(),
                    event.getTransactionTime(),
                    event.getTransactionType(),
                    event.getAccount()
            );

//            Account account = transaction.getAccount();
//            BigDecimal currentBalance = account.getBalance();
//            BigDecimal newBalance = event.getAmount();
            Long accountId = event.getAccount().getAccountNumber();
            Account account = accountService.findAccountById(accountId);
            BigDecimal oldBalance = account.getBalance();
            BigDecimal newBalance = transaction.getAmount();

            if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
                account.setBalance(oldBalance.add(newBalance));
            }
            accountService.saveAccount(account);

            transactionService.saveTransaction(transaction);

            log.info("Completed transaction for account number: {}", account.getAccountNumber());
            acknowledgment.acknowledge();
        } catch (Exception exception) {
            log.error("Error saving transaction: {}", exception.getMessage());
            // TODO handle exception here later
        }
    }
}