package com.bank.webservice.listener;

import com.bank.webservice.controller.AccountController;
import com.bank.webservice.dto.AccountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventListener {

    @Autowired
    private AccountController controller;

    @KafkaListener(topics = "account-details", groupId = "web-service")
    public void handleAccountDetailsEvent(AccountDTO account) {
        log.info("Received account details for account number: {}",
                account.getAccountNumber());
        controller.setAccountDetails(account); // temp solution with tight coupling
    }
}