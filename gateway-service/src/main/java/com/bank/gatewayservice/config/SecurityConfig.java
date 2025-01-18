package com.bank.gatewayservice.config;

import com.bank.gatewayservice.service.FilterServiceImpl;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final FilterServiceImpl filterServiceImpl;
    private final DataSource securityDataSource;

    @Autowired
    public SecurityConfig(FilterServiceImpl filterServiceImpl, DataSource securityDataSource) {
        this.filterServiceImpl = filterServiceImpl;
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
                    .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
                    .authorizeHttpRequests((authorize) -> authorize
                            .requestMatchers("/login", "/verify", "/v3/api-docs/**", "/swagger-ui/**",
                                    "/swagger-ui.html").permitAll().anyRequest().authenticated()
                    )
                    .addFilterBefore(filterServiceImpl, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        } catch (Exception e) {
            log.error("Error configuring security filter chain", e);
            throw new RuntimeException(e);
        }
    }

    @Bean // to allow the Swagger aggregation in the web-service
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:8080"); // Allow requests from web-service
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
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