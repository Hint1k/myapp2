package com.bank.gatewayservice.publisher;

import com.bank.gatewayservice.entity.User;

import java.util.List;

public interface UserEventPublisher {

    void publishUserCreatedEvent(User user);

    void publishAllUsersEvent(List<User> users);
}