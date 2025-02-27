package com.bank.webservice.listener;

import com.bank.webservice.cache.UserCache;
import com.bank.webservice.dto.User;
import com.bank.webservice.event.user.AllUsersEvent;
import com.bank.webservice.event.user.UserCreatedEvent;
import com.bank.webservice.service.LatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserEventListenerTest {

    @Mock
    private LatchService latchService;

    @Mock
    private UserCache userCache;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private UserEventListener userEventListener;

    private UserCreatedEvent userCreatedEvent;
    private AllUsersEvent allUsersEvent;

    @BeforeEach
    void setUp() {
        // Initialize test events
        userCreatedEvent = new UserCreatedEvent(
                1L, 101L, "john doe", "password123"
        );

        allUsersEvent = new AllUsersEvent(List.of(
                new User(1L, 101L, "john doe", "password123"),
                new User(2L, 102L, "jane doe", "password456")
        ));
    }

    @Test
    public void testHandleUserCreatedEvent() {
        // Mock the cache behavior (void method)
        doNothing().when(userCache).addUserToCache(eq(1L), any(User.class));

        // Act
        userEventListener.handleUserCreatedEvent(userCreatedEvent, acknowledgment);

        // Assertions
        assertNotNull(userCreatedEvent, "UserCreatedEvent should not be null");
        assertEquals(1L, userCreatedEvent.getUserId(), "User ID should be 1");

        // Verify
        verify(userCache, times(1)).addUserToCache(eq(1L), any(User.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleAllUsersEvent() {
        // Mock the latch
        when(latchService.getLatch()).thenReturn(new CountDownLatch(1));

        // Mock the cache behavior (void method)
        doNothing().when(userCache).addAllUsersToCache(eq(allUsersEvent.getUsers()));

        // Act
        userEventListener.handleAllUsersEvent(allUsersEvent, acknowledgment);

        // Assertions
        assertNotNull(allUsersEvent, "AllUsersEvent should not be null");
        assertEquals(2, allUsersEvent.getUsers().size(), "There should be 2 users in the event");

        // Verify
        verify(userCache, times(1)).addAllUsersToCache(eq(allUsersEvent.getUsers()));
        verify(acknowledgment, times(1)).acknowledge();
        verify(latchService, times(1)).getLatch();
    }
}