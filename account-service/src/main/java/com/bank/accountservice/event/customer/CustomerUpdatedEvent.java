package com.bank.accountservice.event.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdatedEvent {

    private Long customerId;
    private Long customerNumber;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String accountNumbers;

    // no customer id
    public CustomerUpdatedEvent(Long customerNumber, String name, String email, String phone, String address,
                                String accountNumbers) {
        this.customerNumber = customerNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.accountNumbers = accountNumbers;
    }
}