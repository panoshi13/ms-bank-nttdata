package com.ntt.data.ms.client.delegate;

import com.ntt.data.ms.client.MsClientApiDelegateImpl;
import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.mapper.ClientMapper;
import com.ntt.data.ms.client.model.*;
import com.ntt.data.ms.client.service.CustomerService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MsClientApiDelegateImplTest {
    @Mock
    private CustomerService customerService;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private MsClientApiDelegateImpl msClientApiDelegate;

    @Mock
    private ServerWebExchange exchange;

    private Customer customer;

    private CustomerRequest customerRequest;

    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(new ObjectId("5f50c31f1c9d440000cf41a2"));

        customerRequest = new CustomerRequest();
        customerResponse = new CustomerResponse();
    }

    @Test
    void testGetAllClients() {
        lenient().when(customerService.findAll()).thenReturn(Flux.just(customer));
        lenient().when(clientMapper.mapToCustomerResponse(any(Customer.class))).thenReturn(customerResponse);

        Mono<ResponseEntity<Flux<CustomerResponse>>> result =
            msClientApiDelegate.getAllClients(exchange);

        StepVerifier.create(result)
            .expectNextMatches(response -> response.getBody() != null)
            .verifyComplete();
    }

    @Test
    void testGetCustomerById() {
        when(customerService.findById("123")).thenReturn(Mono.just(customer));
        when(clientMapper.mapToCustomerResponse(any(Customer.class))).thenReturn(customerResponse);

        Mono<ResponseEntity<CustomerResponse>> result =
            msClientApiDelegate.getCustomerById("123", exchange);

        StepVerifier.create(result)
            .expectNextMatches(response -> response.getBody() != null)
            .verifyComplete();
    }

    @Test
    void testCreateClient() {
        when(customerService.create(any(Customer.class))).thenReturn(Mono.just(customer));
        when(clientMapper.mapToCustomer(any(CustomerRequest.class))).thenReturn(customer);
        when(clientMapper.mapToCustomerResponse(any(Customer.class))).thenReturn(customerResponse);

        Mono<ResponseEntity<CustomerResponse>> result =
            msClientApiDelegate.createClient(Mono.just(customerRequest), exchange);

        StepVerifier.create(result)
            .expectNextMatches(response -> response.getBody() != null)
            .verifyComplete();
    }

    @Test
    void testDeleteClient() {
        when(customerService.delete("123")).thenReturn(Mono.just("Cliente eliminado con éxito"));

        Mono<ResponseEntity<InlineResponse200>> result =
            msClientApiDelegate.deleteClient("123", exchange);

        StepVerifier.create(result)
            .expectNextMatches(response -> response.getBody() != null &&
                "Cliente eliminado con éxito".equals(response.getBody().getMessage()))
            .verifyComplete();
    }

    @Test
    void testUpdateClient() {
        when(customerService.update(any(Customer.class))).thenReturn(Mono.just(customer));
        when(clientMapper.mapToCustomer(any(CustomerRequest.class))).thenReturn(customer);
        when(clientMapper.mapToCustomerResponse(any(Customer.class))).thenReturn(customerResponse);

        Mono<ResponseEntity<CustomerResponse>> result =
            msClientApiDelegate.updateClient(Mono.just(customerRequest), exchange);

        StepVerifier.create(result)
            .expectNextMatches(response -> response.getBody() != null)
            .verifyComplete();
    }

    @Test
    void testGetCustomerProductMovements() {
        CustomerProductMovementsResponse movementsResponse = new CustomerProductMovementsResponse();
        when(customerService.getMovement("123", "456")).thenReturn(Mono.just(movementsResponse));

        Mono<ResponseEntity<CustomerProductMovementsResponse>> result =
            msClientApiDelegate.getCustomerProductMovements("123", "456", exchange);

        StepVerifier.create(result)
            .expectNextMatches(response -> response.getBody() != null)
            .verifyComplete();
    }

    @Test
    void testGetCustomerProductBalance() {
        CustomerProductBalanceResponse balanceResponse = new CustomerProductBalanceResponse();
        when(customerService.getBalanceAvailable("123", "456")).thenReturn(
            Mono.just(balanceResponse));

        Mono<ResponseEntity<CustomerProductBalanceResponse>> result =
            msClientApiDelegate.getCustomerProductBalance("123", "456", exchange);

        StepVerifier.create(result)
            .expectNextMatches(response -> response.getBody() != null)
            .verifyComplete();
    }

    @Test
    void testGetMonthlyBalanceReport() {
        MonthlyBalanceReportResponse reportResponse = new MonthlyBalanceReportResponse();
        when(customerService.getMonthlyBalanceReport("123")).thenReturn(Mono.just(reportResponse));

        Mono<ResponseEntity<MonthlyBalanceReportResponse>> result =
            msClientApiDelegate.getMonthlyBalanceReport("123", exchange);

        StepVerifier.create(result)
            .expectNextMatches(response -> response.getBody() != null)
            .verifyComplete();
    }

    @Test
    void testGetConsolidatedSummary() {
        ConsolidatedSummaryResponse summaryResponse = new ConsolidatedSummaryResponse();
        when(customerService.getConsolidatedSummary("123")).thenReturn(Mono.just(summaryResponse));

        Mono<ResponseEntity<ConsolidatedSummaryResponse>> result =
            msClientApiDelegate.getConsolidatedSummary("123", exchange);

        StepVerifier.create(result)
            .expectNextMatches(response -> response.getBody() != null)
            .verifyComplete();
    }
}