package com.bank.accountservice.repository;

import com.bank.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // ?1 represents first argument of the method, i.e. accountNumber
    @Query("SELECT a FROM Account a WHERE a.accountNumber = ?1")
    Account findAccountByItsNumber(Long accountNumber);
}