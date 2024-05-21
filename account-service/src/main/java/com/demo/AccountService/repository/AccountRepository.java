package com.demo.AccountService.repository;

import com.demo.AccountService.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}