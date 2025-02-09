package com.bank.gatewayservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true) // Exclude the role to avoid cyclic references
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ToString.Include
    private Long userId;

    @Column(name = "customer_number", nullable = false)
    @ToString.Include
    private Long customerNumber;

    @Column(name = "username")
    @ToString.Include
    private String username;

    @Column(name = "password")
    @ToString.Include
    private String password;

    @Column(name = "enabled")
    @ToString.Include
    private int isEnabled; // TODO change to boolean type

    // bidirectional, referencing side
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Role role;

    // no userId
    public User(Long customerNumber, String username, String password, int isEnabled, Role role) {
        this.customerNumber = customerNumber;
        this.username = username;
        this.password = password;
        this.isEnabled = isEnabled;
        this.role = role;
    }

    // no userId, no role
    public User(Long customerNumber, String username, String password, int isEnabled) {
        this.customerNumber = customerNumber;
        this.username = username;
        this.password = password;
        this.isEnabled = isEnabled;
    }

    // no userId, no role, no enabled
    public User(Long customerNumber, String username, String password) {
        this.customerNumber = customerNumber;
        this.username = username;
        this.password = password;
    }
}