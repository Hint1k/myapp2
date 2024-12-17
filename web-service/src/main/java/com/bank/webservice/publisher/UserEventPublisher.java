package com.bank.webservice.publisher;

import com.bank.webservice.dto.User;

public interface UserEventPublisher {

    void publishUserRegisteredEvent(User user);

//    void publishAllUsersEvent();
}