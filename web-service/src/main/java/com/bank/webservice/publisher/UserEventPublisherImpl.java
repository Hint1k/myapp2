package com.bank.webservice.publisher;

import com.bank.webservice.dto.User;
import com.bank.webservice.event.user.AllUsersEvent;
import com.bank.webservice.event.user.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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
                user.getCustomerNumber(),
                user.getUsername(),
                user.getPassword()
        );
        kafkaTemplate.send("user-registration-requested", event);
        log.info("Published user-registration-requested event for username: {}", event.getUsername());
    }

    @Override
    public void publishAllUsersEvent() {
        AllUsersEvent event = new AllUsersEvent(new ArrayList<>());
        kafkaTemplate.send("all-users-requested", event);
        log.info("Published all-users-requested event");
    }
}