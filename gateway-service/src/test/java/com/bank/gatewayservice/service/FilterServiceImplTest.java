package com.bank.gatewayservice.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class FilterServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private AccessService accessService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private FilterServiceImpl filterService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Test
    public void testDoFilterInternal_WithTokenMismatchInCache() {
        try {
            String requestToken = "requestToken";
            String cachedToken = "differentToken";

            // Mock RedisTemplate behavior
            ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
            when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);
            when(valueOpsMock.get("token:testUser")).thenReturn(cachedToken);

            // Given: Mock JWT extraction
            when(request.getHeader("Authorization")).thenReturn("Bearer " + requestToken);
            when(jwtService.extractTokenFromHeader("Bearer " + requestToken)).thenReturn(requestToken);
            when(jwtService.extractUsername(requestToken)).thenReturn("testUser");

            // When
            filterService.doFilterInternal(request, response, filterChain);

            // Then: Ensure 401 is returned due to token mismatch
            verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            verify(filterChain, never()).doFilter(request, response);
        } catch (IOException | ServletException e) {
            log.error("testDoFilterInternal_WithTokenMismatchInCache() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testDoFilterInternal_WithInvalidToken() {
        try {
            // Link the mocked PrintWriter to response.getWriter()
            when(response.getWriter()).thenReturn(printWriter);

            // Given: Mock request with an invalid but non-null token
            when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
            when(jwtService.extractTokenFromHeader("Bearer invalidToken")).thenReturn("invalidToken");
            when(jwtService.extractUsername("invalidToken")).thenReturn(null); // Invalid token case

            // When: Call doFilterInternal
            filterService.doFilterInternal(request, response, filterChain);

            // Then: Ensure filter chain is not called and appropriate response is written
            verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(printWriter, times(1)).write("Invalid token");
            verify(filterChain, never()).doFilter(request, response);
        } catch (IOException | ServletException e) {
            log.error("testDoFilterInternal_WithInvalidToken() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testDoFilterInternal_WithNoToken() {
        try {
            // Link the mocked PrintWriter to response.getWriter()
            when(response.getWriter()).thenReturn(printWriter);
            // Given: Mock request with no token
            when(request.getHeader("Authorization")).thenReturn(null);

            // When
            filterService.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(printWriter, times(1)).write("No token provided");
            verify(filterChain, never()).doFilter(request, response);
        } catch (IOException | ServletException e) {
            log.error("testDoFilterInternal_WithNoToken() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testDoFilterInternal_WithValidToken() {
        try {
            String token = "validToken";

            // Mock RedisTemplate's opsForValue().get() behavior
            ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
            when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);
            when(valueOpsMock.get("token:testUser")).thenReturn(token);

            // Mock SecurityContext
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // Given
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractTokenFromHeader("Bearer " + token)).thenReturn(token);
            when(jwtService.extractUsername(token)).thenReturn("testUser");
            when(jwtService.isTokenExpired(token)).thenReturn(false);
            when(jwtService.extractRoles(token)).thenReturn(Collections.singletonList("ROLE_USER"));

            // When
            filterService.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            verify(filterChain, times(1)).doFilter(request, response);
        } catch (IOException | ServletException e) {
            log.error("testDoFilterInternal_WithValidToken() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testDoFilterInternal_WithExpiredToken() {
        try {
            // Mock RedisTemplate's opsForValue().get() behavior
            ValueOperations<String, Object> valueOpsMock = Mockito.mock(String.valueOf(ValueOperations.class));
            when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);
            when(valueOpsMock.get("token:testUser")).thenReturn("expiredToken");

            // Given
            String token = "expiredToken";
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractTokenFromHeader("Bearer " + token)).thenReturn(token);
            when(jwtService.extractUsername(token)).thenReturn("testUser");
            when(jwtService.isTokenExpired(token)).thenReturn(true);

            // When
            filterService.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            verify(filterChain, never()).doFilter(request, response);
        } catch (IOException | ServletException e) {
            log.error("testDoFilterInternal_WithExpiredToken() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testDoFilterInternal_ShouldHandleErrorsGracefully() {
        try {
            // Given
            when(request.getHeader("Authorization")).thenThrow(new RuntimeException("Unexpected error"));

            // When
            filterService.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IOException | ServletException e) {
            log.error("testDoFilterInternal_ShouldHandleErrorsGracefully() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }

    }
}