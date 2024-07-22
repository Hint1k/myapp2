package com.bank.customerservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeletedEvent {
    private Long customerId;
    private Long customerNumber;
}