package com.bank.gatewayservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Entity
@Table(name = "users")
@Setter
@Getter
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isEnabled == user.isEnabled && Objects.equals(userId, user.userId)
                && Objects.equals(customerNumber, user.customerNumber) && Objects.equals(username, user.username)
                && Objects.equals(password, user.password) && Objects.equals(role, user.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, customerNumber, username, password, isEnabled, role);
    }
}