package com.bank.gatewayservice.event;

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
    private String email;
    private String username;
    private String password;
}