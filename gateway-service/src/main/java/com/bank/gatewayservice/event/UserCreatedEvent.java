package com.bank.gatewayservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    private Long userId;
    private Long customerNumber;
    private String username;
    private String password;
}