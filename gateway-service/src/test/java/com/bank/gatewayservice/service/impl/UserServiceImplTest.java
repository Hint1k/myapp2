package com.bank.gatewayservice.service.impl;

import com.bank.gatewayservice.entity.Role;
import com.bank.gatewayservice.entity.User;
import com.bank.gatewayservice.publisher.UserEventPublisher;
import com.bank.gatewayservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        // Initialize test data
        user = new User();
        user.setUserId(1L);
        user.setUsername("testUser");
        user.setPassword("password");
        user.setIsEnabled(1);

        role = new Role();
        role.setUsername("testUser");
        role.setAuthority("ROLE_USER");
        role.setUser(user);

        user.setRole(role);
    }

    @Test
    public void testSaveUser() {
        // Mock the password encoder behavior
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Mock the repository save behavior
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.saveUser(user);

        // Assertions
        assertEquals("encodedPassword", user.getPassword(), "Password should be encoded");
        assertEquals(1, user.getIsEnabled(), "User should be enabled");
        assertNotNull(user.getRole(), "Role should not be null");
        assertEquals("ROLE_USER", user.getRole().getAuthority(),
                "Role authority should be ROLE_USER");

        // Verify
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(user);
        verify(userEventPublisher, times(1)).publishUserCreatedEvent(user);
    }

    @Test
    public void testFindAllUsers() {
        // Mock the repository behavior
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Act
        List<User> users = userService.findAllUsers();

        // Assertions
        assertNotNull(users, "Returned user list should not be null");
        assertEquals(1, users.size(), "There should be 1 user in the list");
        assertEquals("testUser", users.getFirst().getUsername(), "Username should match");

        // Verify
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testFindUserByUsername() {
        // Mock the repository behavior
        when(userRepository.findUserByUsername("testUser")).thenReturn(user);

        // Act
        User foundUser = userService.findUserByUsername("testUser");

        // Assertions
        assertNotNull(foundUser, "Found user should not be null");
        assertEquals("testUser", foundUser.getUsername(), "Username should match");

        // Verify
        verify(userRepository, times(1)).findUserByUsername("testUser");
    }

    @Test
    public void testFindUserById() {
        // TODO: Implement when findUserById logic is added
    }

    @Test
    public void testUpdateUser() {
        // TODO: Implement when updateUser logic is added
    }

    @Test
    public void testDeleteUser() {
        // TODO: Implement when deleteUser logic is added
    }
}