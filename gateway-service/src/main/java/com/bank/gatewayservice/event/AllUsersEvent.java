package com.bank.gatewayservice.event;

import com.bank.gatewayservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllUsersEvent {

    private List<User> users;
}