package com.bank.webservice.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Serializable {

    private Long customerId;

    @NotNull(message = "Customer number is required")
    @Min(value = 1)
    @Digits(integer = MAX_VALUE, fraction = 0, message = "Only whole numbers are allowed")
    private Long customerNumber;

    @NotNull(message = "First Name is required")
    private String firstName;

    @NotNull(message = "Last Name is required")
    private String lastName;

    @NotNull(message = "Middle Name is required")
    private String middleName;

    @NotNull(message = "Email is required")
    private String email;

    @NotNull(message = "Phone is required")
    private String phone;

    @NotNull(message = "Account number is required")
    private List<Long> accountNumbers;

    @NotNull(message = "Address is required")
    private Address address;

    // no customer id
    public Customer(Long customerNumber, String firstName, String lastName, String middleName, String email,
                    String phone, List<Long> accountNumbers, Address address) {
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