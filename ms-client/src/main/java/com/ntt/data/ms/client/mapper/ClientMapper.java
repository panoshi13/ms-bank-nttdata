package com.ntt.data.ms.client.mapper;

import com.ntt.data.ms.client.client.CreditDTO;
import com.ntt.data.ms.client.dto.Charge;
import com.ntt.data.ms.client.dto.Movement;
import com.ntt.data.ms.client.dto.Payment;
import com.ntt.data.ms.client.entity.ClientType;
import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.entity.CustomerType;
import com.ntt.data.ms.client.entity.ProfileType;
import com.ntt.data.ms.client.model.*;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class ClientMapper {

    public CustomerProductMovementsResponseMovementsCredit movementsCredit(CreditDTO creditDTO) {
        List<Payment> payments = creditDTO.getPayments();
        List<Charge> charges = creditDTO.getCharges();

        List<CustomerProductMovementsResponseMovementsCreditPayments> paymentsList = null;
        if (payments != null) {
            paymentsList = payments.stream()
                .map(payment -> {
                    CustomerProductMovementsResponseMovementsCreditPayments paymentsResponse =
                        new CustomerProductMovementsResponseMovementsCreditPayments();
                    paymentsResponse.setAmount(payment.getAmount());
                    paymentsResponse.setDate(payment.getDate().atOffset(ZoneOffset.UTC));
                    return paymentsResponse;
                })
                .toList();
        }

        List<CustomerProductMovementsResponseMovementsCreditCharges> chargesList = null;
        if (charges != null) {
            chargesList = charges.stream()
                .map(charge -> {
                    CustomerProductMovementsResponseMovementsCreditCharges chargesResponse =
                        new CustomerProductMovementsResponseMovementsCreditCharges();
                    chargesResponse.setDescription(charge.getDescription());
                    chargesResponse.setAmount(charge.getAmount());
                    chargesResponse.setDate(charge.getDate().atOffset(ZoneOffset.UTC));
                    return chargesResponse;
                })
                .toList();
        }

        CustomerProductMovementsResponseMovementsCredit movementsCredit =
            new CustomerProductMovementsResponseMovementsCredit();
        movementsCredit.setCharges(chargesList);
        movementsCredit.setPayments(paymentsList);
        return movementsCredit;
    }

    public List<CustomerProductMovementsResponseMovementsBankAccount> movementsBankAccounts(
        List<Movement> movements) {
        return movements.stream()
            .map(movement -> {
                CustomerProductMovementsResponseMovementsBankAccount bankAccount =
                    new CustomerProductMovementsResponseMovementsBankAccount();
                bankAccount.setAmount(movement.getAmount());
                bankAccount.setType(movement.getType());
                bankAccount.setDate(movement.getDate().atOffset(ZoneOffset.UTC));
                return bankAccount;
            })
            .toList();
    }


    public Customer mapToCustomer(CustomerRequest customerRequest) {
        Customer customer = new Customer();
        customer.setId(customerRequest.getId() != null ? new ObjectId(customerRequest.getId()) :
            new ObjectId());
        customer.setName(customerRequest.getName());
        customer.setType(
            new CustomerType(ClientType.valueOf(customerRequest.getType().getCustomerType()),
                ProfileType.valueOf(customerRequest.getType().getProfile())));
        customer.setEmail(customerRequest.getEmail());
        customer.setIdentification(customerRequest.getIdentification());
        customer.setPhone(customerRequest.getPhone());
        customer.setAddress(customerRequest.getAddress());
        return customer;
    }

    public CustomerResponse mapToCustomerResponse(Customer customer) {
        CustomerResponse customerResponse = new CustomerResponse();
        customerResponse.setId(String.valueOf(customer.getId()));
        customerResponse.setName(customer.getName());
        customerResponse.setIdentification(customer.getIdentification());
        customerResponse.setAddress(customer.getAddress());
        customerResponse.setEmail(customer.getEmail());
        CustomerRequestType customerRequestType = new CustomerRequestType();
        customerRequestType.customerType(customer.getType().getCustomerType().name());
        customerRequestType.profile(customer.getType().getProfile().name());
        customerResponse.setType(customerRequestType);
        customerResponse.setPhone(customer.getPhone());
        return customerResponse;
    }
}
