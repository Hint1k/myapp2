package com.bank.customerservice.event.customer;

import com.bank.customerservice.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllCustomersEvent {

   private List<Customer> customers;
}