package com.bank.webservice.event.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private Long userId;
    private String firstName;
    private String lastName;
    private Long customerNumber;
    private String username;
    private String password;
}