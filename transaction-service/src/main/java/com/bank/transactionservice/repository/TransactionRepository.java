package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT DISTINCT a FROM Transaction a WHERE a.accountSourceNumber = ?1 OR a.accountDestinationNumber = ?1")
    List<Transaction> findTransactionsByAccountNumber(Long accountNumber);
}