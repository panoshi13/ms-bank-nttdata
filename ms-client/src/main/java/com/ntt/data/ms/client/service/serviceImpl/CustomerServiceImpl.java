package com.ntt.data.ms.client.service.serviceImpl;

import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.repository.CustomerRepository;
import com.ntt.data.ms.client.service.CustomerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final WebClient bankAccountApiClient;
    private final WebClient creditApiClient;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               @Qualifier("bankAccountApiClient") WebClient bankAccountApiClient,
                               @Qualifier("creditApiClient") WebClient creditApiClient) {
        this.customerRepository = customerRepository;
        this.bankAccountApiClient = bankAccountApiClient;
        this.creditApiClient = creditApiClient;
    }
    @Override
    public Mono<String> create(Customer customer) {
        return customerRepository.save(customer)
                .thenReturn("Cliente guardado con éxito");
    }

    @Override
    public Mono<Customer> findById(String id) {
        return customerRepository.findById(id);
    }

    /*
    public Mono<CustomerDTO> fetchCustomerDetails(String customerId) {
        return customerApiClient.get()
                .uri("/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerDTO.class);
    }

    public Mono<OrderDTO> fetchOrders(String customerId) {
        return orderApiClient.get()
                .uri("/orders/customer/{id}", customerId)
                .retrieve()
                .bodyToMono(OrderDTO.class);
    }

     */

    @Override
    public Flux<Customer> findAll() {

        return customerRepository.findAll();
    }

    @Override
    public Mono<String> update(Customer customer) {
        // Asegurar que el _id sea un ObjectId
        if (customer.getId() == null) {
            return Mono.error(new IllegalArgumentException("El ID no puede ser nulo"));
        }

        return customerRepository.findById(String.valueOf(customer.getId()))
                .flatMap(existingCliente -> {
                    customer.setId(existingCliente.getId()); // Asegurar que se usa el mismo ID
                    return customerRepository.save(customer);
                })
                .thenReturn("Cliente Actualizado con éxito")
                .switchIfEmpty(Mono.error(new RuntimeException("Cliente no encontrado")));
    }
    @Override
    public Mono<String> delete(String id) {
        return customerRepository.deleteById(id)
                .thenReturn("Cliente Eliminado con éxito");
    }
}
