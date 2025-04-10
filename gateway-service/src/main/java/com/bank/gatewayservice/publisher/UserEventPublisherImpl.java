package com.bank.gatewayservice.publisher;

import com.bank.gatewayservice.entity.User;
import com.bank.gatewayservice.event.AllUsersEvent;
import com.bank.gatewayservice.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventPublisherImpl implements UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishUserCreatedEvent(User user) {
        UserCreatedEvent event = new UserCreatedEvent(
                user.getUserId(),
                user.getCustomerNumber(),
                user.getUsername(),
                user.getPassword()
        );
        kafkaTemplate.send("user-created", event);
        log.info("Published user-created event for user id: {}", event.getUserId());
    }

    @Override
    public void publishAllUsersEvent(List<User> users) {
        AllUsersEvent event = new AllUsersEvent(users);
        kafkaTemplate.send("all-users-received", event);
        log.info("Published all-users-received event with {} users", users.size());
    }
}