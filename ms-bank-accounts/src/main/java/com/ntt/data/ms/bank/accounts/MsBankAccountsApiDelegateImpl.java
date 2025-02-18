package com.ntt.data.ms.bank.accounts;


import com.ntt.data.ms.bank.accounts.api.AccountsApiDelegate;
import com.ntt.data.ms.bank.accounts.mapper.BankAccountMapper;
import com.ntt.data.ms.bank.accounts.model.BankAccountResponse;
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
    public Mono<ResponseEntity<BankAccountResponse>> getBankAccountById(String id, ServerWebExchange exchange) {
        return bankAccountService.getBankAccountByProductId(id)
                .map(bankAccount -> ResponseEntity.ok(bankAccountMapper.mapToBankAccountResponse(bankAccount)));
    }

    @Override
    public Mono<ResponseEntity<Flux<BankAccountResponse>>> getBankAccounts(ServerWebExchange exchange) {
        Flux<BankAccountResponse> bankAccountResponses = bankAccountService.getAll()
                .map(bankAccountMapper::mapToBankAccountResponse);

        return Mono.just(ResponseEntity.ok(bankAccountResponses));
    }
}


