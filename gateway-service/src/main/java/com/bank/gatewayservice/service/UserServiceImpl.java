package com.bank.gatewayservice.service;

import com.bank.gatewayservice.entity.Role;
import com.bank.gatewayservice.entity.User;
import com.bank.gatewayservice.publisher.UserEventPublisher;
import com.bank.gatewayservice.repository.RoleRepository;
import com.bank.gatewayservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher publisher;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           UserEventPublisher publisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.publisher = publisher;
    }

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
        publisher.publishUserRegisteredEvent(user);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
//        TODO for future function updates if any
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
//        TODO for future function updates if any
    }

    @Override
    @Transactional
    public List<User> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users;
    }

    @Override
    @Transactional
    public User findUserById(Long userId) {
//      TODO for future function updates if any
        return null;
    }

    @Override
    @Transactional
    public User findUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username);
        return user;
    }
}