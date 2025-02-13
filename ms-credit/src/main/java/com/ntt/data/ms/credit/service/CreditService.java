package com.ntt.data.ms.credit.service;

import com.ntt.data.ms.credit.dto.PaymentDTO;
import com.ntt.data.ms.credit.entity.Credit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditService {
    Flux<Credit> getAll();
    Mono<Credit> create(Credit credit);

    Mono<Credit> paymentCredit(PaymentDTO paymentDTO);
}
