package com.bank.customerservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String houseNumber;
    private String street;
    private String city;
    private String region; // state for USA
    private String postalCode;
    private String country;
}