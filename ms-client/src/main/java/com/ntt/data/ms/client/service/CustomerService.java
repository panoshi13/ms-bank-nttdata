package com.ntt.data.ms.client.service;

import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.model.ConsolidatedSummaryResponse;
import com.ntt.data.ms.client.model.CustomerProductBalanceResponse;
import com.ntt.data.ms.client.model.CustomerProductMovementsResponse;
import com.ntt.data.ms.client.model.MonthlyBalanceReportResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<Customer> create(Customer cliente);

    Mono<Customer> findById(String id);

    Flux<Customer> findAll();

    Mono<Customer> update(Customer cliente);

    Mono<String> delete(String id);

    Mono<CustomerProductMovementsResponse> getMovement(String clientId, String productId);

    Mono<CustomerProductBalanceResponse> getBalanceAvailable(String clientId, String productId);

    Mono<MonthlyBalanceReportResponse> getMonthlyBalanceReport(String clientId);

    Mono<ConsolidatedSummaryResponse> getConsolidatedSummary(String clientId);

    Mono<Customer> getCustomerByDocument(String document);
}
