package com.bank.gatewayservice.publisher;

import com.bank.gatewayservice.entity.User;
import com.bank.gatewayservice.event.AllUsersEvent;
import com.bank.gatewayservice.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserEventPublisherImpl implements UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public UserEventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishUserRegisteredEvent(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getUserId(),
                user.getCustomerNumber(),
                user.getUsername(),
                user.getPassword()
        );
        kafkaTemplate.send("user-registered", event);
        log.info("Published user-registered event for user id: {}", event.getUserId());
        //TODO add check later with completableFuture
    }

    @Override
    public void publishAllUsersEvent(List<User> users) {
        AllUsersEvent event = new AllUsersEvent(users);
        kafkaTemplate.send("all-users-received", event);
        log.info("Published all-users-received event with {} users", users.size());
    }
}