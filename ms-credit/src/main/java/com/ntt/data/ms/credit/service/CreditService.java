package com.ntt.data.ms.credit.service;

import com.ntt.data.ms.credit.dto.PaymentDTO;
import com.ntt.data.ms.credit.dto.SpendDTO;
import com.ntt.data.ms.credit.entity.Credit;
import com.ntt.data.ms.credit.model.InlineResponse200;
import com.ntt.data.ms.credit.model.ThirdPartyPaymentRequest;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditService {
    Flux<Credit> getAll();

    Mono<Credit> create(Credit credit);

    Mono<Credit> paymentCredit(PaymentDTO paymentDTO);

    Mono<Credit> spendCredit(SpendDTO spendDTO);

    Mono<Credit> getCreditById(String id);

    Flux<Credit> getCreditByClient(ObjectId clientId);

    Mono<InlineResponse200> payThirdPartyViaDebitCard(
        Mono<ThirdPartyPaymentRequest> thirdPartyPaymentRequest);

}
