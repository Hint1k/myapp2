package com.bank.webservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    @Pattern(regexp = "^[a-zA-Z]+(\\s[a-zA-Z]+)*$",
            message = "This field must only contain letters and a single space between names")
    private String name;

    @NotNull(message = "Email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$",
            message = "Email format must be name@server.domain, name may include . and _")
    private String email;

    @NotNull(message = "Phone is required")
    @Pattern(regexp = "^\\+\\d{11}$",
            message = "Phone number must contain \"+\" and 11 digits with no spaces or symbols in between")
    private String phone;

    @NotNull(message = "Address is required")
    private String address;

    // A customer may not have any accounts
    private String accountNumbers;

    // no customer id
    public Customer(Long customerNumber, String name, String email, String phone, String address,
                    String accountNumbers) {
        this.customerNumber = customerNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.accountNumbers = accountNumbers;
    }
}