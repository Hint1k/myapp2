package com.bank.accountservice.listener;

import com.bank.accountservice.event.InitialTransactionEvent;
import com.bank.accountservice.service.AccountService;
import com.bank.accountservice.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j // logger
public class TransactionEventListener {

    @Autowired
    private AccountService accountService;

    @KafkaListener(topics = "initial-transaction-made", groupId = "account-service")
    public void handleInitialTransactionEvent(InitialTransactionEvent event, Acknowledgment acknowledgment) {
        log.info("Received initial transaction event for account number: {}", event.getAccountNumber());
        Long accountNumber = event.getAccountNumber();
        BigDecimal newBalance = event.getBalance();

        try {
            Account account = accountService.findAccountByNumber(accountNumber);
            BigDecimal currentBalance = account.getBalance();
            account.setBalance(currentBalance.add(newBalance));

            accountService.updateAccount(account);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error saving initial transaction: {}", e.getMessage());
            // TODO implement error handling later
        }
    }
}