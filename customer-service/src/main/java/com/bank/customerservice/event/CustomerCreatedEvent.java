package com.bank.customerservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent {

    private Long customerId;
    private Long customerNumber;
    private String name;
    private String email;
    private String phone;
    private String address;
    private List<Long> accountNumbers;

    // no customer id
    public CustomerCreatedEvent(Long customerNumber, String name, String email, String phone, String address,
                                List<Long> accountNumbers) {
        this.customerNumber = customerNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.accountNumbers = accountNumbers;
    }
}