package com.ntt.data.ms.client.mapper;

import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.model.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    public CustomerResponse mapToCustomerResponse(Customer customer) {
        CustomerResponse customerResponse = new CustomerResponse();
        customerResponse.setId(String.valueOf(customer.getId()));
        customerResponse.setName(customer.getName());
        customerResponse.setIdentification(customer.getIdentification());
        customerResponse.setAddress(customer.getAddress());
        customerResponse.setEmail(customer.getEmail());
        customerResponse.setType(customer.getType());
        customerResponse.setPhone(customer.getPhone());
        return customerResponse;
    }


}
