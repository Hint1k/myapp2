package com.bank.webservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private Long userId;
    private String firstName;
    private String lastName;
    private Long customerNumber;

    @NotNull(message = "Username should not be empty")
    private String username;

    @NotNull(message = "Password should not be empty")
    private String password;

    // no userId
    public User(String firstName, String lastName, Long customerNumber, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.customerNumber = customerNumber;
        this.username = username;
        this.password = password;
    }
}