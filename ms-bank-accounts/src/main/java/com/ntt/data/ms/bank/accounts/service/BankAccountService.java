package com.ntt.data.ms.bank.accounts.service;

import com.ntt.data.ms.bank.accounts.dto.DepositDTO;
import com.ntt.data.ms.bank.accounts.entity.BankAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankAccountService {
    Flux<BankAccount> getAll();
    Mono<BankAccount> create(BankAccount bankAccount);
    Mono<String> update(BankAccount bankAccount);
    Mono<String> delete(String id);
    Mono<BankAccount> depositBankAccount(DepositDTO depositDTO);
    Mono<BankAccount> withdrawBankAccount(DepositDTO depositDTO);
    Mono<BankAccount> getBankAccountByProductId(String productId);
}
