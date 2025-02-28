package com.ntt.data.ms.credit.service;

import com.ntt.data.ms.credit.client.ClientDTO;
import com.ntt.data.ms.credit.client.ClientType;
import com.ntt.data.ms.credit.client.CustomerTypeDTO;
import com.ntt.data.ms.credit.client.ProfileType;
import com.ntt.data.ms.credit.config.CustomException;
import com.ntt.data.ms.credit.dto.PaymentDTO;
import com.ntt.data.ms.credit.dto.SpendDTO;
import com.ntt.data.ms.credit.entity.Credit;
import com.ntt.data.ms.credit.entity.CreditType;
import com.ntt.data.ms.credit.repository.CreditRepository;
import com.ntt.data.ms.credit.service.serviceImpl.CreditServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CreditServiceImplTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private CreditServiceImpl creditService;

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
    void getDataClientReturnsClientDTO() {
        String clientId = "5f50c31f1c9d440000cf41a2";
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(new ObjectId(clientId));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(Function.class));
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClientDTO.class)).thenReturn(Mono.just(clientDTO));

        Mono<ClientDTO> result = creditService.getDataClient(clientId);

        StepVerifier.create(result)
            .expectNextMatches(client -> client.getId().equals(new ObjectId(clientId)))
            .verifyComplete();
    }

    @Test
    void getDataClientReturnsEmptyWhenClientNotFound() {
        String clientId = "5f50c31f1c9d440000cf41a2";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(Function.class));
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClientDTO.class)).thenReturn(Mono.empty());

        Mono<ClientDTO> result = creditService.getDataClient(clientId);

        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void createCreditThrowsExceptionWhenClientNotFound() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(Function.class));
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClientDTO.class)).thenReturn(Mono.empty());

        Mono<Credit> result = creditService.create(new Credit());

        StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof CustomException &&
                throwable.getMessage().equals("El cliente no existe"))
            .verify();
    }

    @Test
    void createCreditThrowsExceptionWhenCreditTypeMismatch() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setType(new CustomerTypeDTO(ClientType.BUSINESS, ProfileType.NORMAL));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(Function.class));
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClientDTO.class)).thenReturn(Mono.just(clientDTO));

        Credit credit = new Credit();
        credit.setType(CreditType.PERSONAL);

        Mono<Credit> result = creditService.create(credit);

        StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof CustomException &&
                throwable.getMessage()
                    .equals("El tipo de crédito no coincide con el tipo de cliente"))
            .verify();
    }

    @Test
    void createCreditThrowsExceptionWhenClientHasActivePersonalCredit() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setType(new CustomerTypeDTO(ClientType.PERSONAL, ProfileType.NORMAL));
        String clientId = "5f50c31f1c9d440000cf41a2";
        clientDTO.setId(new ObjectId(clientId));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(Function.class));
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClientDTO.class)).thenReturn(Mono.just(clientDTO));
        when(creditRepository.countByClientIdAndType(isNull(), eq(CreditType.PERSONAL))).thenReturn(
            Mono.just(1L));

        Credit credit = new Credit();
        credit.setType(CreditType.PERSONAL);
        credit.setBalance(2000.0);
        credit.setTermMonths(12);

        Mono<Credit> result = creditService.create(credit);

        StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof CustomException &&
                throwable.getMessage()
                    .equals("Un cliente personal solo puede tener un crédito activo"))
            .verify();
    }

    @Test
    void paymentCreditThrowsExceptionWhenAccountNotFound() {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setIdCard("5f50c31f1c9d440000cf41a2");

        when(creditRepository.findById(paymentDTO.getIdCard())).thenReturn(Mono.empty());

        Mono<Credit> result = creditService.paymentCredit(paymentDTO);

        StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof CustomException &&
                throwable.getMessage().equals("Cuenta no encontrada"))
            .verify();
    }

    @Test
    void spendCreditThrowsExceptionWhenAccountNotFound() {
        SpendDTO spendDTO = new SpendDTO();
        spendDTO.setIdCard("5f50c31f1c9d440000cf41a2");

        when(creditRepository.findById(spendDTO.getIdCard())).thenReturn(Mono.empty());

        Mono<Credit> result = creditService.spendCredit(spendDTO);

        StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof CustomException &&
                throwable.getMessage().equals("Cuenta no encontrada"))
            .verify();
    }


}