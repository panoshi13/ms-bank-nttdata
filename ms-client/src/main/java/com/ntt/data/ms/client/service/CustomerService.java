package com.ntt.data.ms.client.service;

import com.ntt.data.ms.client.entity.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<String> create(Customer cliente);
    Mono<Customer> findById(String id);
    Flux<Customer> findAll();
    Mono<String> update(Customer cliente);
    Mono<String> delete(String id);
}
