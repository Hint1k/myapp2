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

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Email is required")
    private String email;

    @NotNull(message = "Phone is required")
    private String phone;

    @NotNull(message = "Address is required")
    private String address;

    @NotNull(message = "Account number is required")
    private List<Long> accountNumbers;

    // no customer id
    public Customer(Long customerNumber, String name, String email, String phone, String address,
                    List<Long> accountNumbers) {
        this.customerNumber = customerNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.accountNumbers = accountNumbers;
    }
}