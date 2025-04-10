package com.bank.gatewayservice.publisher;

import com.bank.gatewayservice.entity.User;

public interface UserEventPublisher {

    void publishUserCreatedEvent(User user);
}