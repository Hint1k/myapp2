package com.bank.gatewayservice.config;

import com.bank.gatewayservice.filter.JwtFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final DataSource securityDataSource;

    @Autowired
    public SecurityConfig(JwtFilter jwtFilter, DataSource securityDataSource) {
        this.jwtFilter = jwtFilter;
        this.securityDataSource = securityDataSource;
    }

    @Bean
    public JdbcUserDetailsManager user() {
        return new JdbcUserDetailsManager(securityDataSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests((authorize) -> authorize
                            .requestMatchers("/admin/**").hasRole("ADMIN")
                            .requestMatchers("/manager/**").hasRole("MANAGER")
                            .requestMatchers("/user/**").hasRole("USER")
                            .requestMatchers("/login", "/verify").permitAll()
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        } catch (Exception e) {
            log.error("Error configuring security filter chain", e);
            throw new RuntimeException(e);
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) {
        AuthenticationManagerBuilder authenticationManager = http.getSharedObject(AuthenticationManagerBuilder.class);
        try {
            authenticationManager.jdbcAuthentication()
                    .dataSource(securityDataSource).passwordEncoder(passwordEncoder());
            return authenticationManager.build();
        } catch (Exception e) {
            log.error("Error in AuthenticationManager", e);
            throw new RuntimeException(e);
        }
    }
}