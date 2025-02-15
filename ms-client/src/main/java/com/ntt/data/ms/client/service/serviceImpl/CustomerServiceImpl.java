package com.ntt.data.ms.client.service.serviceImpl;

import com.ntt.data.ms.client.client.BankAccountDTO;
import com.ntt.data.ms.client.client.CreditDTO;
import com.ntt.data.ms.client.config.CustomException;
import com.ntt.data.ms.client.dto.BalanceAvailableDTO;
import com.ntt.data.ms.client.dto.Movement;
import com.ntt.data.ms.client.dto.ProductDTO;
import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.repository.CustomerRepository;
import com.ntt.data.ms.client.service.CustomerService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@Slf4j
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
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException("No se encontraron resultados")));
    }

    public Mono<CreditDTO> fetchCredit(String productId) {
        return creditApiClient.get()
                .uri("/credits/{id}", productId)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == 400,
                        clientResponse -> Mono.error(new RuntimeException("Error 400 en consulta de movimientos de tarjeta")))
                .bodyToMono(CreditDTO.class);
    }

    public Mono<BankAccountDTO> fetchBankAccount(String productId) {
        return bankAccountApiClient.get()
                .uri("/accounts/bank/{id}", productId)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == 400,
                        clientResponse -> Mono.error(new CustomException("Error 400 en consulta de movimientos de tarjeta")))
                .bodyToMono(BankAccountDTO.class);
    }


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

    @Override
    public Mono<ProductDTO> getMovement(String clientId, String productId) {
        return customerRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new RuntimeException("Cliente no encontrado")))
                .flatMap(customer -> getProductDTOMono(productId, customer));

    }

    @Override
    public Mono<BalanceAvailableDTO> getBalanceAvailable(String clientId, String productId) {
        return null;
    }

    private Mono<ProductDTO> getProductDTOMono(String productId, Customer customer) {
        Mono<BankAccountDTO> bankAccountDTOMono = fetchBankAccount(productId)
                .onErrorResume(throwable -> {
                    log.error("Error al obtener movimientos de cuenta bancaria: {}", throwable.getMessage());
                    return Mono.empty();
                });
        Mono<CreditDTO> creditDTOMono = fetchCredit(productId)
                .onErrorResume(throwable -> {
                    log.error("Error al obtener movimientos de cuenta de crédito: {}", throwable.getMessage());
                    return Mono.empty();
                });

        return Mono.zip(
                        bankAccountDTOMono.hasElement(),
                        creditDTOMono.hasElement()
                )
                .flatMap(hasElements -> {
                    boolean hasBankAccount = hasElements.getT1();
                    boolean hasCreditAccount = hasElements.getT2();
                    if (!hasBankAccount && !hasCreditAccount) {
                        return Mono.error(new CustomException("No se pudieron obtener los movimientos"));
                    }
                    return Mono.zip(
                                    bankAccountDTOMono.defaultIfEmpty(new BankAccountDTO()),
                                    creditDTOMono.defaultIfEmpty(new CreditDTO())
                            )
                            .map(tuple -> {
                                BankAccountDTO bankAccountDTO = tuple.getT1();
                                CreditDTO creditDTO = tuple.getT2();
                                return new ProductDTO(
                                        customer.getName(),
                                        customer.getIdentification(),
                                        bankAccountDTO.getMovements(),
                                        creditDTO
                                );
                            });
                });
    }



}
