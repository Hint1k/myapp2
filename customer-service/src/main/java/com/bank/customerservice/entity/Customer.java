package com.bank.customerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "customer")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long customerId;

    @Column(name = "customer_number", nullable = false, unique = true)
    private Long customerNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "account_numbers") // customer may not have any accounts
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId)
                && Objects.equals(customerNumber, customer.customerNumber)
                && Objects.equals(name, customer.name) && Objects.equals(email, customer.email)
                && Objects.equals(phone, customer.phone) && Objects.equals(address, customer.address)
                && Objects.equals(accountNumbers, customer.accountNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, customerNumber, name, email, phone, address, accountNumbers);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", customerNumber=" + customerNumber +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", accountNumbers='" + accountNumbers + '\'' +
                '}';
    }
}