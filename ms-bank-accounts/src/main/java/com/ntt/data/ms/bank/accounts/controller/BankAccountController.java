package com.ntt.data.ms.bank.accounts.controller;


import com.ntt.data.ms.bank.accounts.dto.DepositDTO;
import com.ntt.data.ms.bank.accounts.entity.BankAccount;
import com.ntt.data.ms.bank.accounts.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @GetMapping("/bank")
    public Flux<BankAccount> getAllBankAccounts() {
        return bankAccountService.getAll();
    }

    @PostMapping("/register")
    public Mono<BankAccount> createBankAccount(@RequestBody BankAccount bankAccount) {
        return bankAccountService.create(bankAccount);
    }

    @PostMapping("/deposit")
    public Mono<BankAccount> depositBankAccount(@RequestBody DepositDTO depositDTO) {
        return bankAccountService.depositBankAccount(depositDTO);
    }

    @PostMapping("/withdraw")
    public Mono<BankAccount> crearCredito(@RequestBody DepositDTO depositDTO) {
        return bankAccountService.withdrawBankAccount(depositDTO);
    }

}

