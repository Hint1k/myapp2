package com.bank.webservice.cache;

import com.bank.webservice.dto.User;

import java.util.List;

public interface UserCache {

    void addUserToCache(Long userId, User user);

    void addAllUsersToCache(List<User> users);

    void updateUserInCache(Long userId, User user);

    void deleteUserFromCache(Long userId);

    List<User> getAllUsersFromCache();

    User getUserFromCache(Long userId);
}