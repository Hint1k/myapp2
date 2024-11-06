package com.bank.gatewayservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "authorities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long roleId;

    @NotNull
    @Column(name = "username")
    private String username;

    @NotNull
    @Column(name = "authority")
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