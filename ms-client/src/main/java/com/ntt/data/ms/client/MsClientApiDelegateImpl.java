package com.ntt.data.ms.client;

import com.ntt.data.ms.client.api.CustomersApiDelegate;
import com.ntt.data.ms.client.mapper.ClientMapper;
import com.ntt.data.ms.client.model.*;
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


    @Override
    public Mono<ResponseEntity<CustomerResponse>> createClient(Mono<CustomerRequest> customerRequest, ServerWebExchange exchange) {
        return customerRequest
                .map(clientMapper::mapToCustomer)
                .flatMap(customerService::create)
                .map(customer -> ResponseEntity.ok(clientMapper.mapToCustomerResponse(customer)));
    }

    @Override
    public Mono<ResponseEntity<InlineResponse200>> deleteClient(String id, ServerWebExchange exchange) {
        return customerService.delete(id)
                .flatMap(s -> {
                    InlineResponse200 message = new InlineResponse200();
                    message.setMessage(s);
                    return Mono.just(ResponseEntity.ok(message));
                });
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateClient(Mono<CustomerRequest> customerRequest, ServerWebExchange exchange) {
        return customerRequest
                .map(clientMapper::mapToCustomer)
                .flatMap(customerService::update)
                .map(customer -> ResponseEntity.ok(clientMapper.mapToCustomerResponse(customer)));
    }

    @Override
    public Mono<ResponseEntity<CustomerProductMovementsResponse>> getCustomerProductMovements(String customerId, String productId, ServerWebExchange exchange) {
        return customerService.getMovement(customerId,productId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CustomerProductBalanceResponse>> getCustomerProductBalance(String customerId, String cardId, ServerWebExchange exchange) {
        return customerService.getBalanceAvailable(customerId,cardId)
                .map(ResponseEntity::ok);
    }
}
