package com.demo.TransactionService.repository;

import com.demo.TransactionService.dto.AccountDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountDTO, Long> {
}