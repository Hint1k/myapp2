package com.bank.gatewayservice.listener;

import com.bank.gatewayservice.entity.User;
import com.bank.gatewayservice.event.UserCreatedEvent;
import com.bank.gatewayservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventListener {

    private final UserService service;

    @KafkaListener(topics = "user-creation-requested", groupId = "gateway-service")
    public void handleUserCreatedEvent(UserCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received user-creation-requested event for username: {}", event.getUsername());
        User user = new User(
                event.getCustomerNumber(),
                event.getUsername(),
                event.getPassword()
        );
        service.saveUser(user);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "all-users-requested", groupId = "gateway-service")
    public void handleAllUsersEvent(Acknowledgment acknowledgment) {
        log.info("Received all-users-requested event");
        try {
            service.findAllUsers();
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error finding all users: {}", e.getMessage());
            // TODO implement error handling later
        }
    }
}