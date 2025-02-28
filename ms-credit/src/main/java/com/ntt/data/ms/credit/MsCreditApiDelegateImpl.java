package com.ntt.data.ms.credit;


import com.ntt.data.ms.credit.api.CreditsApiDelegate;
import com.ntt.data.ms.credit.mapper.CreditMapper;
import com.ntt.data.ms.credit.model.*;
import com.ntt.data.ms.credit.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MsCreditApiDelegateImpl implements CreditsApiDelegate {
    private final CreditService creditService;

    private final CreditMapper creditMapper;

    @Override
    public Mono<ResponseEntity<CreditResponse>> registerSpend(
        Mono<SpendRequest> spendRequest, ServerWebExchange exchange) {
        return spendRequest
            .map(creditMapper::mapToSpendDTO)
            .flatMap(creditService::spendCredit)
            .map(credit -> ResponseEntity.ok(creditMapper.mapToCreditResponse(credit)));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> registerPayment(
        Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest
            .map(creditMapper::mapToPaymentDTO)
            .flatMap(creditService::paymentCredit)
            .map(credit -> ResponseEntity.ok(creditMapper.mapToCreditResponse(credit)));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> registerCredit(
        Mono<CreditRequest> creditRequest, ServerWebExchange exchange) {
        return creditRequest
            .map(creditMapper::mapToCredit)
            .flatMap(creditService::create)
            .map(credit -> ResponseEntity.ok(creditMapper.mapToCreditResponse(credit)));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> getCreditById(
        String id, ServerWebExchange exchange) {
        return creditService.getCreditById(id)
            .map(credit -> ResponseEntity.ok(creditMapper.mapToCreditResponse(credit)));
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getClients(
        ServerWebExchange exchange) {
        return creditService.getAll()
            .collectList()
            .map(credits -> {
                Flux<CreditResponse> creditResponses = Flux.fromIterable(credits)
                    .map(creditMapper::mapToCreditResponse);
                return ResponseEntity.ok(creditResponses);
            })
            .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getCreditByClientId(
        String id, ServerWebExchange exchange) {
        return creditService.getCreditByClient(new ObjectId(id))
            .collectList()
            .map(credits -> {
                Flux<CreditResponse> creditResponses = Flux.fromIterable(credits)
                    .map(creditMapper::mapToCreditResponse);
                return ResponseEntity.ok(creditResponses);
            })
            .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<InlineResponse200>> payThirdPartyViaDebitCard(
        Mono<ThirdPartyPaymentRequest> thirdPartyPaymentRequest, ServerWebExchange exchange) {
        return creditService.payThirdPartyViaDebitCard(thirdPartyPaymentRequest)
            .map(ResponseEntity::ok);
    }
}
