package com.ntt.data.ms.bank.accounts.service;

import com.ntt.data.ms.bank.accounts.client.dto.*;
import com.ntt.data.ms.bank.accounts.config.CustomException;
import com.ntt.data.ms.bank.accounts.entity.BankAccount;
import com.ntt.data.ms.bank.accounts.repository.BankAccountRepository;
import com.ntt.data.ms.bank.accounts.service.serviceImpl.BankAccountServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    @Qualifier("creditApiClient")
    private WebClient creditApiClient;

    @Mock
    @Qualifier("customerApiClient")
    private WebClient customerApiClient;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

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
    void testGetAll() {
        BankAccount account = new BankAccount();
        when(bankAccountRepository.findAll()).thenReturn(Flux.just(account));

        StepVerifier.create(bankAccountService.getAll())
            .expectNext(account)
            .verifyComplete();
    }

    @Test
    void testGetBankAccountsByClientId() {
        BankAccount account = new BankAccount();
        when(bankAccountRepository.findByClientId(any(ObjectId.class))).thenReturn(Flux.just(account));

        StepVerifier.create(bankAccountService.getBankAccountsByClientId("60d5ec49f1e4e2a3d4f1e4e2"))
            .expectNext(account)
            .verifyComplete();
    }

    @Test
    void testCreateBankAccountWithValidClient() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setClientId(new ObjectId("60d5ec49f1e4e2a3d4f1e4e2"));
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setType(new CustomerTypeDTO(ClientType.PERSONAL, ProfileType.NORMAL));

        when(customerApiClient.get().uri(anyString()).retrieve().bodyToMono(ClientDTO.class)).thenReturn(Mono.just(clientDTO));
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(Mono.just(bankAccount));

        StepVerifier.create(bankAccountService.create(bankAccount))
            .expectNext(bankAccount)
            .verifyComplete();
    }

    @Test
    void testCreateBankAccountWithInvalidClient() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setClientId(new ObjectId("60d5ec49f1e4e2a3d4f1e4e2"));

        when(customerApiClient.get().uri(anyString()).retrieve().bodyToMono(ClientDTO.class)).thenReturn(Mono.empty());

        StepVerifier.create(bankAccountService.create(bankAccount))
            .expectError(CustomException.class)
            .verify();
    }

    @Test
    void testFetchCreditByClientId() {
        String clientId = "5f50c31f1c9d440000cf41a2";
        CreditDTO creditDTO = new CreditDTO();
        creditDTO.setStatus(true);

        when(creditApiClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(Function.class));
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(CreditDTO.class)).thenReturn(Flux.just(new CreditDTO()));

        StepVerifier.create(bankAccountService.fetchCreditByClientId(clientId))
            .expectNextMatches(credits -> credits.size() == 1 && credits.get(0).equals(creditDTO))
            .verifyComplete();
    }
}