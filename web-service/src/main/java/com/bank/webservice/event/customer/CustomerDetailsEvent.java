package com.bank.webservice.event.customer;

import com.bank.webservice.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailsEvent extends BaseEvent implements CustomerEvent {

    private Long customerId;
    private Long customerNumber;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String accountNumbers;
}