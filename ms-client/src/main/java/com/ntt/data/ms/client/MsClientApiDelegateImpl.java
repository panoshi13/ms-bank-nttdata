package com.ntt.data.ms.client;

import com.ntt.data.ms.client.api.CustomersApiDelegate;
import com.ntt.data.ms.client.mapper.ClientMapper;
import com.ntt.data.ms.client.model.CustomerResponse;
import com.ntt.data.ms.client.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MsClientApiDelegateImpl implements CustomersApiDelegate {

    private final CustomerService customerService;
    private final ClientMapper clientMapper;

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponse>>> getAllClients(ServerWebExchange exchange) {
        return Mono.just(
                ResponseEntity.ok(
                        customerService.findAll().map(clientMapper::mapToCustomerResponse
                        )));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(String id, ServerWebExchange exchange) {
        return customerService.findById(id)
                .map(customer -> ResponseEntity.ok(clientMapper.mapToCustomerResponse(customer)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
