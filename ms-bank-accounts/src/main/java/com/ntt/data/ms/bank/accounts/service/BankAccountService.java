package com.ntt.data.ms.bank.accounts.service;

import com.ntt.data.ms.bank.accounts.entity.BankAccount;
import com.ntt.data.ms.bank.accounts.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankAccountService {
    Flux<BankAccount> getAll();

    Flux<BankAccount> getBankAccountsByClientId(String clientId);

    Mono<BankAccount> create(BankAccount bankAccount);

    Mono<String> update(BankAccount bankAccount);

    Mono<String> delete(String id);

    Mono<BankAccount> depositBankAccount(TransactionRequest depositDTO);

    Mono<BankAccount> withdrawBankAccount(TransactionRequest depositDTO);

    Mono<BankAccount> getBankAccountByProductId(String productId);

    Mono<InlineResponse200> transferBetweenAccounts(Mono<TransferRequest> requestMono);

    Mono<ReportCommissionResponse> getCommissionReport(String startDate, String endDate);

    Mono<InlineResponse2001> createDebitCard(Mono<DebitCardRequest> debitCardRequest);

    Mono<InlineResponse2002> associateDebitCard(Mono<DebitCardAssociationRequest> debitCardAssociationRequest);
}
