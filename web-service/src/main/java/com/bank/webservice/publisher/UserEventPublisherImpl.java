//package com.bank.webservice.publisher;
//
//import com.bank.webservice.dto.User;
//import com.bank.webservice.event.user.AllUsersEvent;
//import com.bank.webservice.event.user.UserCreatedEvent;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//
//@Component
//@Slf4j
//public class UserEventPublisherImpl implements UserEventPublisher {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    @Autowired
//    public UserEventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    @Override
//    public void publishUserCreatedEvent(User user) {
//        UserCreatedEvent event = new UserCreatedEvent(
//                user.getCustomerNumber(),
//                user.getUsername(),
//                user.getPassword()
//        );
//        kafkaTemplate.send("user-creation-requested", event);
//        log.info("Published user-creation-requested event for username: {}", event.getUsername());
//    }
//
//    @Override
//    public void publishAllUsersEvent() {
//        AllUsersEvent event = new AllUsersEvent(new ArrayList<>());
//        kafkaTemplate.send("all-users-requested", event);
//        log.info("Published all-users-requested event");
//    }
//}