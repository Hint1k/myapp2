package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

//    @Query("SELECT t FROM Transaction t WHERE t.accountDestinationNumber = ?1")
    public List<Transaction> findTransactionsByAccountDestinationNumber(Long accountNumber);
}