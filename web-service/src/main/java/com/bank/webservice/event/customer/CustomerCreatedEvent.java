package com.bank.webservice.event.customer;

import com.bank.webservice.dto.Address;
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
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String phone;
//    private Address address;
//    private List<Long> accountNumbers;

    // no customer id
    public CustomerCreatedEvent(Long customerNumber, String firstName, String lastName, String middleName,
                                String email, String phone
//            , Address address
//        List<Long> accountNumbers
    ) {
        this.customerNumber = customerNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.email = email;
        this.phone = phone;
//        this.address = address;
        //        this.accountNumbers = accountNumbers;
    }
}