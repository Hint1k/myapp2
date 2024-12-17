package com.bank.gatewayservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userId;

    @NotNull(message = "First name should not be empty")
    @Column(name = "first_name")
// TODO add checks for capital letter
    private String firstName;

    @NotNull(message = "Last name should not be empty")
    @Column(name = "last_name")
// TODO add checks for capital letter
    private String lastName;

    @NotNull(message = "Email should not be empty")
    @Column(name = "email")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$",
            message = "Email format must be name@server.domain")
    private String email;

    @Column(name = "username")
    @NotNull(message = "Username should not be empty")
    private String username;

    @Column(name = "password")
    @NotNull(message = "Password should not be empty")
    private String password;

    @Column(name = "enabled")
    private int isEnabled; // TODO change to boolean type

    // bidirectional, referencing side
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Role role;

    // no userId
    public User(String firstName, String lastName, String email, String username, String password, int isEnabled,
                Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.isEnabled = isEnabled;
        this.role = role;
    }

    // no userId, no role
    public User(String firstName, String lastName, String email, String username, String password, int isEnabled) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.isEnabled = isEnabled;
    }

    // no userId, no role, no enabled
    public User(String firstName, String lastName, String email, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
    }
}