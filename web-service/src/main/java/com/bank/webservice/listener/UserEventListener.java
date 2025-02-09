package com.bank.webservice.listener;

import com.bank.webservice.cache.UserCache;
import com.bank.webservice.dto.User;
import com.bank.webservice.event.user.AllUsersEvent;
import com.bank.webservice.event.user.UserRegisteredEvent;
import com.bank.webservice.service.LatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class UserEventListener {

    private final LatchService latch;
    private final UserCache cache;

    @Autowired
    public UserEventListener(LatchService latch, UserCache cache) {
        this.latch = latch;
        this.cache = cache;
    }

    @KafkaListener(topics = "user-registered", groupId = "web-service")
    public void handleUserRegisteredEvent(UserRegisteredEvent event, Acknowledgment acknowledgment) {
        User user = new User(
                event.getUserId(),
                event.getCustomerNumber(),
                event.getUsername(),
                event.getPassword()
        );
        log.info("Received user-registered event with user id: {}", event.getUserId());
        cache.addUserToCache(user.getUserId(), user);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "all-users-received", groupId = "web-service")
    public void handleAllUsersEvent(AllUsersEvent event, Acknowledgment acknowledgment) {
        List<User> users = event.getUsers();
        log.info("Received all-users-received event with {} users", users.size());
        cache.addAllUsersToCache(users);
        CountDownLatch latch = this.latch.getLatch(); // latch initialisation is in RegistrationController class
        if (latch != null) {
            latch.countDown();
        }
        acknowledgment.acknowledge();
    }
}