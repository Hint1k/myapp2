package com.bank.gatewayservice.service;

import com.bank.gatewayservice.entity.User;

import java.util.List;

public interface UserService {

    void saveUser(User user);

    List<User> findAllUsers();

    User findUserByUsername(String username);
}