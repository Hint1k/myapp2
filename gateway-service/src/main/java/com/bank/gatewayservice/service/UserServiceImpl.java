package com.bank.gatewayservice.service;

import com.bank.gatewayservice.entity.Role;
import com.bank.gatewayservice.entity.User;
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
    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void saveUser(User user) {
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        user.setIsEnabled(1);
        String username = user.getUsername();

        Role role = new Role();
        role.setUser(user);
        role.setUsername(username);
        role.setAuthority("ROLE_CUSTOMER"); // TODO add different user roles later
        user.setRole(role);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
//        TODO add later
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
//        TODO add later
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
//      TODO add later
        return null;
    }

    @Override
    public User findUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username);
        return user;
    }
}