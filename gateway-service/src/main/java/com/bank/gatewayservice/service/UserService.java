package com.bank.gatewayservice.service;

import com.bank.gatewayservice.entity.User;

import java.util.List;

public interface UserService {

    void saveUser(User user);

    void updateUser(User user);

    void deleteUser(Long userId);

    List<User> findAllUsers();

    User findUserById(Long userId);

    User findUserByUsername(String username);
}