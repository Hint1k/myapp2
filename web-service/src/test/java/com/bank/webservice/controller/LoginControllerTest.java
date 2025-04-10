package com.bank.webservice.controller;

import com.bank.webservice.service.FilterService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoginController.class)
@AutoConfigureMockMvc
@Slf4j
public class LoginControllerTest {

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private HttpSession session;

    @MockBean
    private FilterService filterService;

    @Autowired
    private MockMvc mockMvc;

    private static final String LOGIN_URL = "http://gateway-service:8080/login";

    @Test
    public void testLogin_SuccessfulLogin() {
        ResponseEntity<Map<String, String>> mockResponse = ResponseEntity.ok(Map.of("token", "mockToken"));

        // Mock the response of RestTemplate
        Mockito.when(restTemplate.exchange(
                Mockito.eq(LOGIN_URL),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.<ParameterizedTypeReference<Map<String, String>>>any()
        )).thenReturn(mockResponse);

        try {
            mockMvc.perform(post("/login")
                            .param("username", "testUser")
                            .param("password", "testPass"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"));
        } catch (Exception e) {
            log.error("testLogin_SuccessfulLogin fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        // Verify exchange call
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(
                Mockito.eq(LOGIN_URL),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.<ParameterizedTypeReference<Map<String, String>>>any()
        );
    }

    @Test
    public void testLogin_InvalidLogin() {
        // Mock unauthorized exception with custom headers and body
        HttpHeaders headers = new HttpHeaders();
        byte[] body = new byte[0];

        // Use HttpClientErrorException.create to construct the exception
        HttpClientErrorException unauthorizedException = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                headers,
                body,
                Charset.defaultCharset()
        );

        // Mock the response of RestTemplate
        Mockito.when(restTemplate.exchange(
                Mockito.eq(LOGIN_URL),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenThrow(unauthorizedException);

        try {
            mockMvc.perform(post("/login")
                            .param("username", "wrongUser")
                            .param("password", "wrongPass"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("errorMessage"))
                    .andExpect(model().attribute("errorMessage", "Invalid credentials"));
        } catch (Exception e) {
            log.error("testLogin_InvalidLogin fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        // Verify exchange call
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(
                Mockito.eq(LOGIN_URL),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.<ParameterizedTypeReference<Map<String, Object>>>any()
        );
    }

    @Test
    public void testLogin_NoTokenReceived() {
        // Mock response that doesn't contain a token
        ResponseEntity<Map<String, Object>> mockResponse = ResponseEntity.ok(Map.of());

        // Mock the response of RestTemplate
        Mockito.when(restTemplate.exchange(
                Mockito.eq(LOGIN_URL),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(mockResponse);

        try {
            // Perform the POST request to /login with any username and password
            mockMvc.perform(post("/login")
                            .param("username", "testUser")
                            .param("password", "testPass"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("errorMessage"))
                    .andExpect(model().attribute("errorMessage",
                            "You don't have a jwt token. Try to login again."));

        } catch (Exception e) {
            log.error("testLogin_NoTokenRecieved fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        // Verify exchange call
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(
                Mockito.eq(LOGIN_URL),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.<ParameterizedTypeReference<Map<String, Object>>>any()
        );
    }
}