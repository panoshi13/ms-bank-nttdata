package com.ntt.data.ms.bank.accounts.service;

import com.ntt.data.ms.bank.accounts.client.dto.ClientDTO;
import com.ntt.data.ms.bank.accounts.client.dto.CreditDTO;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceImplTest {
    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    @Qualifier("customerApiClient")
    private WebClient customerApiClient;

    @Mock
    @Qualifier("creditApiClient")
    private WebClient creditApiClient;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    private WebClient.RequestHeadersSpec requestHeadersSpec;

    private WebClient.ResponseSpec responseSpec;

    private BankAccount testAccount;

    private ClientDTO testClient;

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
        when(bankAccountRepository.findByClientId(any(ObjectId.class))).thenReturn(
            Flux.just(account));

        StepVerifier.create(
                bankAccountService.getBankAccountsByClientId("60d5ec49f1e4e2a3d4f1e4e2"))
            .expectNext(account)
            .verifyComplete();
    }


}