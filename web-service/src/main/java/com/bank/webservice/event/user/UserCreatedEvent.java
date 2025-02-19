package com.bank.webservice.event.user;

import com.bank.webservice.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent extends BaseEvent {

    private Long userId;
    private Long customerNumber;
    private String username;
    private String password;

    // no userId
    public UserCreatedEvent(Long customerNumber, String username, String password) {
        this.customerNumber = customerNumber;
        this.username = username;
        this.password = password;
    }
}