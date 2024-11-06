package com.bank.gatewayservice.repository;

import com.bank.gatewayservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    // ?1 represents first argument of the method, i.e. username
    @Query("SELECT a FROM User a WHERE a.username = ?1")
    User findUserByUsername(String username);
}