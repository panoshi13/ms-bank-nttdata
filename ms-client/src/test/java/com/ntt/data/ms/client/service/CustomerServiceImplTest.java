package com.ntt.data.ms.client.service;

import com.ntt.data.ms.client.client.BankAccountDTO;
import com.ntt.data.ms.client.client.CreditDTO;
import com.ntt.data.ms.client.config.CustomException;
import com.ntt.data.ms.client.entity.ClientType;
import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.entity.CustomerType;
import com.ntt.data.ms.client.entity.ProfileType;
import com.ntt.data.ms.client.mapper.ClientMapper;
import com.ntt.data.ms.client.model.CustomerProductBalanceResponse;
import com.ntt.data.ms.client.model.CustomerProductMovementsResponse;
import com.ntt.data.ms.client.model.MonthlyBalanceReportResponse;
import com.ntt.data.ms.client.repository.CustomerRepository;
import com.ntt.data.ms.client.service.serviceImpl.CustomerServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.mockito.Mockito.when;


public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    @Qualifier("bankAccountApiClient")
    private WebClient bankAccountApiClient;

    @Mock
    private WebClient creditApiClient;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private BankAccountDTO bankAccountDTO;
    private CreditDTO creditDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customer = new Customer(new ObjectId("67b602e73bc3a372425b3d64"),
            new CustomerType(ClientType.PERSONAL, ProfileType.NORMAL), "Harold Alfaro", "76123198",
            "937186093", "harold@example.com", "av. principal 112");
        bankAccountApiClient = WebClient.builder().baseUrl("http://localhost:8080").build();
        creditApiClient = WebClient.builder().baseUrl("http://localhost:8082").build();
        customerService = new CustomerServiceImpl(customerRepository, bankAccountApiClient, creditApiClient, clientMapper);
        bankAccountDTO = new BankAccountDTO();
        creditDTO = new CreditDTO();
    }

    @Test
    public void testCreate() {
        when(customerRepository.save(customer)).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.create(customer))
            .expectNext(customer)
            .verifyComplete();
    }

    @Test
    public void testFindById() {
        String id = "67b602e73bc3a372425b3d64";
        when(customerRepository.findById(id)).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.findById(id))
            .expectNext(customer)
            .verifyComplete();
    }

    @Test
    public void testFindByIdNotFound() {
        String id = "nonexistentId";
        when(customerRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(customerService.findById(id))
            .expectErrorMatches(throwable -> throwable instanceof CustomException &&
                throwable.getMessage().equals("No se encontraron resultados"))
            .verify();
    }
    /*
    @Test
    public void testFetchCredit() {
        String productId = "product123";
        when(creditApiClient.get()
            .uri("/credits/{id}", productId)
            .retrieve()
            .bodyToMono(CreditDTO.class)).thenReturn(Mono.just(creditDTO));

        StepVerifier.create(customerService.fetchCredit(productId))
            .expectNext(creditDTO)
            .verifyComplete();
    }

     */

    /*
    @Test
    public void testFetchBankAccount() {
        String productId = "product123";
        when(bankAccountApiClient.get()
            .uri("/accounts/bank/{id}", productId)
            .retrieve()
            .bodyToMono(BankAccountDTO.class)).thenReturn(Mono.just(bankAccountDTO));

        StepVerifier.create(customerService.fetchBankAccount(productId))
            .expectNext(bankAccountDTO)
            .verifyComplete();
    }

     */

    @Test
    public void testFindAll() {
        when(customerRepository.findAll()).thenReturn(Flux.just(customer));

        StepVerifier.create(customerService.findAll())
            .expectNext(customer)
            .verifyComplete();
    }

    /*
    @Test
    public void testUpdate() {
        when(customerRepository.findById(String.valueOf(customer.getId()))).thenReturn(Mono.just(customer));
        when(customerRepository.save(customer)).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.update(customer))
            .expectNext(customer)
            .verifyComplete();
    }

     */
    /*
    @Test
    public void testUpdateCustomerNotFound() {
        when(customerRepository.findById(String.valueOf(customer.getId()))).thenReturn(Mono.empty());

        StepVerifier.create(customerService.update(customer))
            .expectErrorMatches(throwable -> throwable instanceof CustomException &&
                throwable.getMessage().equals("Cliente no encontrado"))
            .verify();
    }

     */

    @Test
    public void testDelete() {
        String id = "67b602e73bc3a372425b3d64";
        when(customerRepository.existsById(id)).thenReturn(Mono.just(true));
        when(customerRepository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(customerService.delete(id))
            .expectNext("Cliente eliminado con éxito")
            .verifyComplete();
    }

    @Test
    public void testDeleteCustomerNotFound() {
        String id = "nonexistentId";
        when(customerRepository.existsById(id)).thenReturn(Mono.just(false));

        StepVerifier.create(customerService.delete(id))
            .expectNext("Cliente no encontrado")
            .verifyComplete();
    }

    /*
    @Test
    public void testGetMovement() {
        String clientId = "67b602e73bc3a372425b3d64";
        String productId = "product123";
        CustomerProductMovementsResponse movementsResponse = new CustomerProductMovementsResponse();

        when(customerRepository.findById(clientId)).thenReturn(Mono.just(customer));
        when(customerService.getProductDTOMono(productId, customer)).thenReturn(Mono.just(movementsResponse));

        StepVerifier.create(customerService.getMovement(clientId, productId))
            .expectNext(movementsResponse)
            .verifyComplete();
    }



    @Test
    public void testGetBalanceAvailable() {
        String clientId = "67b602e73bc3a372425b3d64";
        String productId = "product123";
        CustomerProductBalanceResponse balanceResponse = new CustomerProductBalanceResponse();

        when(customerRepository.findById(clientId)).thenReturn(Mono.just(customer));
        when(customerService.getCustomerProductBalanceResponse(productId, customer)).thenReturn(Mono.just(balanceResponse));

        StepVerifier.create(customerService.getBalanceAvailable(clientId, productId))
            .expectNext(balanceResponse)
            .verifyComplete();
    }

    @Test
    public void testGetMonthlyBalanceReport() {
        String clientId = "67b602e73bc3a372425b3d64";
        MonthlyBalanceReportResponse reportResponse = new MonthlyBalanceReportResponse();

        when(customerService.fetchBankAccountByClientId(clientId)).thenReturn(Flux.just(bankAccountDTO));
        when(customerService.fetchCreditByClientId(clientId)).thenReturn(Flux.just(creditDTO));

        StepVerifier.create(customerService.getMonthlyBalanceReport(clientId))
            .expectNextMatches(response -> response.getClientId().equals(clientId))
            .verifyComplete();
    }
    */
}