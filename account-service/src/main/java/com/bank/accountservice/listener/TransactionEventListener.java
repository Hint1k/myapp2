package com.bank.accountservice.listener;

import com.bank.accountservice.event.TransactionCreatedEvent;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.util.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class TransactionEventListener {

    private final BalanceService balanceService;

    @Autowired
    public TransactionEventListener(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @KafkaListener(topics = "transaction-created", groupId = "account-service")
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event, Acknowledgment acknowledgment) {
        Long accountSourceNumber = event.getAccountSourceNumber();
        Long accountDestinationNumber = event.getAccountDestinationNumber();
        BigDecimal amount = event.getAmount();
        Long transactionId = event.getTransactionId();
        TransactionType transactionType = event.getTransactionType();

        //TODO implement transaction status - PENDING, APPROVED, FAILED
        balanceService.updateAccountBalance(accountSourceNumber, accountDestinationNumber, amount, transactionId,
                transactionType);

        log.info("Received transaction-created event for transaction id: {}", transactionId);
        acknowledgment.acknowledge();
    }
}