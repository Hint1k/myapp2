package com.bank.webservice.event.customer;

import com.bank.webservice.dto.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdatedEvent {

    // TODO remove the fields that cannot be updated later
    private Long customerId;
    private Long customerNumber;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String phone;
    private List<Long> accountNumbers;
    private Address address;

    // no customer id
    public CustomerUpdatedEvent(Long customerNumber, String firstName, String lastName, String middleName,
                                String email, String phone, List<Long> accountNumbers, Address address) {
        this.customerNumber = customerNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.email = email;
        this.phone = phone;
        this.accountNumbers = accountNumbers;
        this.address = address;
    }
}