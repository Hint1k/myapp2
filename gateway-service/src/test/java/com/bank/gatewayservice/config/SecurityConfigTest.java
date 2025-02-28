package com.bank.gatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.sql.DataSource;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Mock
    private DataSource securityDataSource;

    @Mock
    private HttpSecurity httpSecurity;

    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    public void testUserDetailsManagerBean() {
        JdbcUserDetailsManager userDetailsManager = securityConfig.user();
        assertNotNull(userDetailsManager, "JdbcUserDetailsManager should not be null");
    }

    @Test
    public void testPasswordEncoderBean() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertNotNull(passwordEncoder, "PasswordEncoder should not be null");
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder,
                "PasswordEncoder should be BCryptPasswordEncoder");
    }

    @Test
    public void testSecurityFilterChainBean() {
        // Deep stubbing to simulate method chaining
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        // Call method under test
        SecurityFilterChain securityFilterChain = securityConfig.securityFilterChain(httpSecurity);

        // Verify the result
        assertNotNull(securityFilterChain, "SecurityFilterChain should not be null");
    }

    @Test
    public void testCorsConfigurationSourceBean() {
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        assertNotNull(corsConfigurationSource, "CorsConfigurationSource should not be null");

        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(corsConfig, "CorsConfiguration should not be null");
        assertTrue(Objects.requireNonNull(corsConfig.getAllowedOrigins()).contains("http://localhost:8080"),
                "Allowed origin should include 'http://localhost:8080'");
    }

    @Test
    public void testAuthenticationManagerBean() {
        try {
            // Given: Mock AuthenticationManagerBuilder and related methods
            AuthenticationManagerBuilder authenticationManagerBuilder = mock(AuthenticationManagerBuilder.class);
            JdbcUserDetailsManagerConfigurer<AuthenticationManagerBuilder> jdbcConfigurer =
                    mock(JdbcUserDetailsManagerConfigurer.class);

            // Simulate the chaining behavior of jdbcAuthentication()
            when(authenticationManagerBuilder.jdbcAuthentication()).thenReturn(jdbcConfigurer);
            when(jdbcConfigurer.dataSource(securityDataSource)).thenReturn(jdbcConfigurer);
            when(authenticationManagerBuilder.build()).thenReturn(authenticationManager);

            // Mock httpSecurity.getSharedObject to return the mocked AuthenticationManagerBuilder
            when(httpSecurity.getSharedObject(AuthenticationManagerBuilder.class))
                    .thenReturn(authenticationManagerBuilder);

            // When: Calling authenticationManager method
            AuthenticationManager result = securityConfig.authenticationManager(httpSecurity);

            // Then: Verify that the result is not null and the method calls were made as expected
            assertNotNull(result, "AuthenticationManager should not be null");
            verify(httpSecurity, times(1)).getSharedObject(AuthenticationManagerBuilder.class);
            verify(authenticationManagerBuilder, times(1)).jdbcAuthentication();
            verify(jdbcConfigurer, times(1)).dataSource(securityDataSource);
            verify(authenticationManagerBuilder, times(1)).build();
        } catch (Exception e) {
            log.error("testAuthenticationManagerBean() failed: {}", e.getMessage());
            fail("Serialization/deserialization should not throw an exception: " + e.getMessage());
        }
    }
}