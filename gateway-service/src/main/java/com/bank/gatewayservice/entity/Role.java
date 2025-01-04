package com.bank.gatewayservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "authorities")
@Data
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

    // no roleId
    public Role(String username, String authority, User user) {
        this.username = username;
        this.authority = authority;
        this.user = user;
    }

    // no roleId, no user
    public Role(String username, String authority) {
        this.username = username;
        this.authority = authority;
    }
}