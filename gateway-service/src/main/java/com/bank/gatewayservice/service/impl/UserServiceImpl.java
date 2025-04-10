package com.bank.gatewayservice.service.impl;

import com.bank.gatewayservice.entity.Role;
import com.bank.gatewayservice.entity.User;
import com.bank.gatewayservice.publisher.UserEventPublisher;
import com.bank.gatewayservice.repository.UserRepository;
import com.bank.gatewayservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher publisher;

    @Override
    @Transactional
    public void saveUser(User user) {
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        user.setIsEnabled(1);
        String username = user.getUsername();

        Role role = new Role();
        role.setUsername(username);
        role.setAuthority("ROLE_USER");
        role.setUser(user);

        user.setRole(role);

        userRepository.save(user);
        publisher.publishUserCreatedEvent(user);
    }

    @Override
    @Transactional
    public List<User> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users;
    }

    @Override
    @Transactional
    public User findUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username);
        return user;
    }
}