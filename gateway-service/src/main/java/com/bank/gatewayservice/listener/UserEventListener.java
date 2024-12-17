package com.bank.gatewayservice.listener;

import com.bank.gatewayservice.entity.User;
import com.bank.gatewayservice.event.UserRegisteredEvent;
import com.bank.gatewayservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventListener {

    private final UserService service;

    @Autowired
    public UserEventListener(UserService service) {
        this.service = service;
    }

    @KafkaListener(topics = "user-registration-requested", groupId = "gateway-service")
    public void handleUserCreatedEvent(UserRegisteredEvent event, Acknowledgment acknowledgment) {
        log.info("Received user-registration-requested event for username: {}", event.getUsername());
        User user = new User(
                event.getFirstName(),
                event.getLastName(),
                event.getEmail(),
                event.getUsername(),
                event.getPassword()
        );
        service.saveUser(user);
        acknowledgment.acknowledge();
    }
}