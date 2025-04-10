package com.bank.webservice.service.impl;

import com.bank.webservice.dto.UserResponse;
import com.bank.webservice.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerExceptionResolver;
import com.bank.webservice.exception.UnauthorizedException;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class FilterServiceImplTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private HandlerExceptionResolver exceptionResolver;

    @InjectMocks
    private FilterServiceImpl filterService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain filterChain;

    @Test
    public void testDoFilterInternal_WithValidToken() {
        // Given: Mock session with a valid token
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("jwtToken")).thenReturn("validToken");
        when(tokenService.validateToken("validToken"))
                .thenReturn(new UserResponse("testUser", List.of("ROLE_USER")));

        // When: Calling doFilterInternal
        filterService.doFilterInternal(request, response, filterChain);

        // Then: Ensure filterChain.doFilter is called
        try {
            verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
        } catch (ServletException | IOException e) {
            log.error("testDoFilterInternal_WithValidToken() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDoFilterInternal_WithNoSession() {
        // Given: No session available
        when(request.getSession(false)).thenReturn(null);

        // When: Calling doFilterInternal
        filterService.doFilterInternal(request, response, filterChain);

        // Then: Ensure redirect to login
        try {
            verify(response, times(1)).sendRedirect("/index");
        } catch (IOException e) {
            log.error("testDoFilterInternal_WithNoSession() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDoFilterInternal_WithInvalidToken() {
        // Given: Mock session with an invalid token
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("jwtToken")).thenReturn("invalidToken");
        when(tokenService.validateToken("invalidToken")).thenReturn(null);

        // When: Calling doFilterInternal
        filterService.doFilterInternal(request, response, filterChain);

        // Then: Ensure redirect to login
        try {
            verify(response, times(1)).sendRedirect("/index");
        } catch (IOException e) {
            log.error("testDoFilterInternal_WithInvalidToken() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDoFilterInternal_UnauthorizedAccess() {
        // Given: Mock a valid session with a valid token
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("jwtToken")).thenReturn("validToken");

        // Simulate a case when token is valid but the user has no access rights = Unauthorized Exception
        when(tokenService.validateToken("validToken")).thenThrow(new UnauthorizedException("Access denied"));

        // When: Calling doFilterInternal
        filterService.doFilterInternal(request, response, filterChain);

        // Then: Ensure redirection to /access-denied
        try {
            verify(response, times(1)).sendRedirect("/access-denied");
        } catch (IOException e) {
            log.error("testDoFilterInternal_UnauthorizedAccess() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDoFilterInternal_WithExceptionHandling() {
        // Given: Simulating an exception during filter execution
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("jwtToken")).thenReturn("validToken");
        when(tokenService.validateToken("validToken")).thenThrow(new RuntimeException("Token validation error"));

        // When: Calling doFilterInternal
        filterService.doFilterInternal(request, response, filterChain);

        // Then: Ensure exception is handled properly
        verify(exceptionResolver, times(1))
                .resolveException(eq(request), eq(response), isNull(), any(RuntimeException.class));
    }

    @Test
    public void testShouldNotFilter_WithExcludedEndpoints() {
        // Given: Mock request to an excluded endpoint
        when(request.getRequestURI()).thenReturn("/index");

        // When: Call shouldNotFilter
        boolean result = filterService.shouldNotFilter(request);

        // Then: Ensure it returns true
        assert result : "shouldNotFilter should return true for excluded endpoints";
    }
}