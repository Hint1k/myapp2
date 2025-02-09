package com.bank.webservice.event.user;

import com.bank.webservice.dto.User;
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