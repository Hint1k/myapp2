package com.bank.customerservice.repository;

import com.bank.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ?1 represents first argument of the method, i.e. customerNumber
    @Query("SELECT a FROM Customer a WHERE a.customerNumber = ?1")
    Customer findCustomerByItsNumber(Long customerNumber);
}