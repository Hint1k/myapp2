package com.bank.webservice.event.customer;

import com.bank.webservice.dto.Customer;
import com.bank.webservice.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllCustomersEvent extends BaseEvent {

   private List<Customer> customers;
}