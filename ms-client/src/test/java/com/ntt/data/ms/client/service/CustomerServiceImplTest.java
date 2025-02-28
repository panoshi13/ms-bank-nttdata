package com.ntt.data.ms.client.service;

import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.mapper.ClientMapper;
import com.ntt.data.ms.client.repository.CustomerRepository;
import com.ntt.data.ms.client.service.serviceImpl.CustomerServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    @Qualifier("bankAccountApiClient")
    private WebClient bankAccountApiClient;

    @Mock
    @Qualifier("creditApiClient")
    private WebClient creditApiClient;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    private WebClient.RequestHeadersSpec requestHeadersSpec;

    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);
    }

    @Test
    public void testCreate() {
        Customer customer = new Customer();
        customer.setId(new ObjectId("5f50c31f1c9d440000cf41a2"));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));

        Mono<Customer> result = customerService.create(customer);

        StepVerifier.create(result)
            .expectNext(customer)
            .verifyComplete();
    }

    @Test
    public void testFindById() {
        Customer customer = new Customer();
        customer.setId(new ObjectId("5f50c31f1c9d440000cf41a2"));
        when(customerRepository.findById("123")).thenReturn(Mono.just(customer));

        Mono<Customer> result = customerService.findById("123");

        StepVerifier.create(result)
            .expectNext(customer)
            .verifyComplete();
    }

    @Test
    public void testUpdate() {
        Customer customer = new Customer();
        customer.setId(new ObjectId("5f50c31f1c9d440000cf41a2"));
        when(customerRepository.findById(String.valueOf(new ObjectId("5f50c31f1c9d440000cf41a2")))).thenReturn(Mono.just(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));

        Mono<Customer> result = customerService.update(customer);

        StepVerifier.create(result)
            .expectNext(customer)
            .verifyComplete();
    }

    @Test
    public void testDelete() {
        when(customerRepository.existsById("123")).thenReturn(Mono.just(true));
        when(customerRepository.deleteById("123")).thenReturn(Mono.empty());

        Mono<String> result = customerService.delete("123");

        StepVerifier.create(result)
            .expectNext("Cliente eliminado con éxito")
            .verifyComplete();
    }
}