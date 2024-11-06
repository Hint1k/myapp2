package com.bank.gatewayservice.repository;

import com.bank.gatewayservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}