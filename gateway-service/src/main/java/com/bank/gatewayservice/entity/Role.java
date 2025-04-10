package com.bank.gatewayservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Entity
@Table(name = "authorities")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true) // Exclude the user to avoid cyclic references
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ToString.Include
    private Long roleId;

    @NotNull
    @Column(name = "username")
    @ToString.Include
    private String username;

    @NotNull
    @Column(name = "authority")
    @ToString.Include
    private String authority;

    // bidirectional, owning side
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(roleId, role.roleId) && Objects.equals(username, role.username)
                && Objects.equals(authority, role.authority) && Objects.equals(user, role.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, username, authority, user);
    }
}