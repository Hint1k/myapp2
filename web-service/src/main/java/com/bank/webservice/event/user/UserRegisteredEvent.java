package com.bank.webservice.event.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private Long userId;
    private Long customerNumber;
    private String username;
    private String password;

    // no userId
    public UserRegisteredEvent(Long customerNumber, String username, String password) {
        this.customerNumber = customerNumber;
        this.username = username;
        this.password = password;
    }
}