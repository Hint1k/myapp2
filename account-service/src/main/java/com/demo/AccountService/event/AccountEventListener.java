package com.demo.AccountService.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AccountEventListener {

    @EventListener
    public void handleAccountCreatedEvent(AccountCreatedEvent event) {
        // Handle the event (e.g., create initial transaction, log the event, etc.)
        System.out.println("Received AccountCreatedEvent: " + event.toString()); // temp code
        // Add event handling logic here
    }
}