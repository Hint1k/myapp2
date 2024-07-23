package com.bank.webservice.event.customer;

import com.bank.webservice.dto.Customer;
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