package com.ntt.data.ms.bank.accounts;


import com.ntt.data.ms.bank.accounts.api.AccountsApiDelegate;
import com.ntt.data.ms.bank.accounts.mapper.BankAccountMapper;
import com.ntt.data.ms.bank.accounts.model.*;
import com.ntt.data.ms.bank.accounts.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class MsBankAccountsApiDelegateImpl implements AccountsApiDelegate {
    private final BankAccountService bankAccountService;

    private final BankAccountMapper bankAccountMapper;

    @Override
    public Mono<ResponseEntity<BankAccountResponse>> getBankAccountById(
        String id,
        ServerWebExchange exchange) {
        return bankAccountService.getBankAccountByProductId(id)
            .map(bankAccount -> ResponseEntity.ok(
                bankAccountMapper.mapToBankAccountResponse(bankAccount)));
    }

    @Override
    public Mono<ResponseEntity<Flux<BankAccountResponse>>> getBankAccounts(
        ServerWebExchange exchange) {
        Flux<BankAccountResponse> bankAccountResponses = bankAccountService.getAll()
            .map(bankAccountMapper::mapToBankAccountResponse);

        return Mono.just(ResponseEntity.ok(bankAccountResponses));
    }


    @Override
    public Mono<ResponseEntity<BankAccountResponse>> registerBankAccount(
        Mono<InlineObject> inlineObject, ServerWebExchange exchange) {
        return inlineObject
            .map(bankAccountMapper::mapToBankAccount)
            .flatMap(bankAccountService::create)
            .map(bankAccount -> ResponseEntity.ok(bankAccountMapper.accountResponse(bankAccount)));
    }


    @Override
    public Mono<ResponseEntity<BankAccountResponse>> withdrawFromBankAccount(
        Mono<TransactionRequest> transactionRequest, ServerWebExchange exchange) {
        return transactionRequest
            .flatMap(bankAccountService::withdrawBankAccount)
            .map(bankAccount -> ResponseEntity.ok(bankAccountMapper.accountResponse(bankAccount)));
    }

    @Override
    public Mono<ResponseEntity<BankAccountResponse>> depositToBankAccount(
        Mono<TransactionRequest> transactionRequest, ServerWebExchange exchange) {
        return transactionRequest
            .flatMap(bankAccountService::depositBankAccount)
            .map(bankAccount -> ResponseEntity.ok(bankAccountMapper.accountResponse(bankAccount)));
    }

    @Override
    public Mono<ResponseEntity<InlineResponse200>> transferBetweenAccounts(
        Mono<TransferRequest> transferRequest, ServerWebExchange exchange) {
        return transferRequest
            .flatMap(request -> bankAccountService.transferBetweenAccounts(Mono.just(request))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                    .body(new InlineResponse200().message(e.getMessage()))))
            );
    }

    @Override
    public Mono<ResponseEntity<Flux<BankAccountResponse>>> getBankAccountsByClientId(
        String id,
        ServerWebExchange exchange) {
        Flux<BankAccountResponse> bankAccountResponses =
            bankAccountService.getBankAccountsByClientId(id)
                .map(bankAccountMapper::mapToBankAccountResponse);

        return Mono.just(ResponseEntity.ok(bankAccountResponses));
    }


    @Override
    public Mono<ResponseEntity<ReportCommissionResponse>> getCommissionReport(
        String startDate,
        String endDate,
        ServerWebExchange exchange) {
        return bankAccountService.getCommissionReport(startDate, endDate)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<InlineResponse2001>> createDebitCard(
        Mono<DebitCardRequest> debitCardRequest, ServerWebExchange exchange) {
        return bankAccountService.createDebitCard(debitCardRequest)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<InlineResponse2002>> associateDebitCard(
        Mono<DebitCardAssociationRequest> debitCardAssociationRequest, ServerWebExchange exchange) {
        return bankAccountService.associateDebitCard(debitCardAssociationRequest)
            .map(ResponseEntity::ok);
    }
}



