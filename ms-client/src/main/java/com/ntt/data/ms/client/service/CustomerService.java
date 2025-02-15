package com.ntt.data.ms.client.service;

import com.ntt.data.ms.client.dto.BalanceAvailableDTO;
import com.ntt.data.ms.client.dto.Movement;
import com.ntt.data.ms.client.dto.ProductDTO;
import com.ntt.data.ms.client.entity.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<String> create(Customer cliente);
    Mono<Customer> findById(String id);
    Flux<Customer> findAll();
    Mono<String> update(Customer cliente);
    Mono<String> delete(String id);
    Mono<ProductDTO> getMovement(String clientId, String productId);
    Mono<BalanceAvailableDTO> getBalanceAvailable(String clientId, String productId);
}
