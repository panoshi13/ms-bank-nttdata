package com.ntt.data.ms.credit.controller;

import com.ntt.data.ms.credit.dto.PaymentDTO;
import com.ntt.data.ms.credit.entity.Credit;
import com.ntt.data.ms.credit.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
public class CreditController {
    private final CreditService creditService;

    @GetMapping("")
    public Flux<Credit> getAllCredits() {
        return creditService.getAll();
    }

    @PostMapping("/register")
    public Mono<Credit> createCredit(@RequestBody Credit credit) {
        return creditService.create(credit);
    }

    @PostMapping("/payments")
    public Mono<Credit> paymentCredit(@RequestBody PaymentDTO paymentDTO) {
        return creditService.paymentCredit(paymentDTO);
    }

}
