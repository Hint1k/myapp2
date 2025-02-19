package com.bank.webservice.event.customer;

public interface CustomerEvent {

    Long getCustomerId();

    Long getCustomerNumber();

    String getName();

    String getEmail();

    String getPhone();

    String getAddress();

    String getAccountNumbers();
}