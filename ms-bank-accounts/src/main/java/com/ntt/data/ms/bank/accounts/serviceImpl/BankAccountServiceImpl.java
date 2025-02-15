package com.ntt.data.ms.bank.accounts.serviceImpl;

import com.ntt.data.ms.bank.accounts.client.dto.ClientDTO;
import com.ntt.data.ms.bank.accounts.client.dto.ClientType;
import com.ntt.data.ms.bank.accounts.config.CustomException;
import com.ntt.data.ms.bank.accounts.dto.DepositDTO;
import com.ntt.data.ms.bank.accounts.entity.AccountType;
import com.ntt.data.ms.bank.accounts.entity.BankAccount;
import com.ntt.data.ms.bank.accounts.entity.Holder;
import com.ntt.data.ms.bank.accounts.entity.Movement;
import com.ntt.data.ms.bank.accounts.repository.BankAccountRepository;
import com.ntt.data.ms.bank.accounts.service.BankAccountService;
import com.ntt.data.ms.bank.accounts.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@Service
public class BankAccountServiceImpl implements BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final WebClient webClient;


    public BankAccountServiceImpl(BankAccountRepository bankAccountRepository,
                                  WebClient.Builder webClientBuilder,
                                  @Value("${api.client.url}") String clientUrl) {
        this.bankAccountRepository = bankAccountRepository;
        this.webClient = webClientBuilder.baseUrl(clientUrl).build();
    }

    @Override
    public Flux<BankAccount> getAll() {
        return bankAccountRepository.findAll();
    }

    public Mono<ClientDTO> getData(String id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/customers/{id}").build(id)) // Path variable
                .retrieve()
                .bodyToMono(ClientDTO.class);
    }

    @Override
    public Mono<BankAccount> create(BankAccount bankAccount) {
        return getData(String.valueOf(bankAccount.getClientId()))
                .switchIfEmpty(Mono.error(new CustomException("El cliente no existe")))
                .flatMap(client -> validateClientType(String.valueOf(client.getType())))
                .flatMap(clientType -> validateClientAccounts(bankAccount, clientType))
                .flatMap(this::configureAccount)
                .flatMap(bankAccountRepository::save);
    }

    // Metodo para validar el tipo de cliente
    private Mono<ClientType> validateClientType(String clientType) {
        try {
            return Mono.just(ClientType.valueOf(clientType.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Mono.error(new CustomException("Tipo de cliente no válido: " + clientType));
        }
    }

    //  Metodo para validar si el cliente puede tener la cuenta solicitada
    private Mono<BankAccount> validateClientAccounts(BankAccount bankAccount, ClientType clientType) {
        return bankAccountRepository.findByClientId(bankAccount.getClientId())
                .collectList()
                .flatMap(accounts -> {
                    if (!canHaveAccount(bankAccount.getType(), clientType, accounts, bankAccount.getHolders())) {
                        return Mono.error(new CustomException("El cliente no puede tener este tipo de cuenta."));
                    }
                    return Mono.just(bankAccount);
                });
    }

    //  Lógica centralizada de validación
    private boolean canHaveAccount(AccountType accountType, ClientType clientType, List<BankAccount> accounts, List<Holder> holders) {
        boolean hasSavingsAccount = accounts.stream().anyMatch(acc -> acc.getType() == AccountType.SAVINGS);
        boolean hasCurrentAccount = accounts.stream().anyMatch(acc -> acc.getType() == AccountType.CURRENT);

        if (clientType == ClientType.PERSONAL) {
            return !(accountType == AccountType.SAVINGS && hasSavingsAccount) &&
                    !(accountType == AccountType.CURRENT && hasCurrentAccount);
        }

        if (clientType == ClientType.BUSINESS) {
            if (accountType == AccountType.SAVINGS || accountType == AccountType.FIXED_TERM) {
                return false;
            }
            return holders != null && !holders.isEmpty();
        }

        return true;
    }

    //  Metodo para configurar valores predeterminados de la cuenta bancaria
    private Mono<BankAccount> configureAccount(BankAccount bankAccount) {
        bankAccount.setOpeningDate(LocalDateTime.now());
        bankAccount.setBalance(0.00);

        switch (bankAccount.getType()) {
            case SAVINGS:
                bankAccount.setMaintenance(0.00);
                bankAccount.setLimitMovements(5);
                break;
            case CURRENT:
                bankAccount.setMaintenance(10.00);
                bankAccount.setLimitMovements(null);
                break;
            case FIXED_TERM:
                bankAccount.setMaintenance(0.00);
                bankAccount.setLimitMovements(1);
                bankAccount.setDayWithdrawalDeposit(ThreadLocalRandom.current().nextInt(1, 29));
                break;
            default:
                return Mono.error(new CustomException("Tipo de cuenta no válido."));
        }

        return Mono.just(bankAccount);
    }



    @Override
    public Mono<String> update(BankAccount bankAccount) {
        return null;
    }

    @Override
    public Mono<String> delete(String id) {
        return bankAccountRepository.deleteById(id).thenReturn("Cuenta Bancaria Eliminada");
    }

    @Override
    public Mono<BankAccount> depositBankAccount(DepositDTO depositDTO) {
        return bankAccountRepository.findById(depositDTO.getId())
                .switchIfEmpty(Mono.error(new CustomException("Cuenta no encontrada")))
                .flatMap(bankAccount -> {
                    if (bankAccount.getLimitMovements() == 0){
                        return Mono.error(new CustomException("Agotaste los movimientos del mes"));
                    }

                    if (bankAccount.getType() == AccountType.FIXED_TERM){
                        if (!Util.isDayOfMoth(bankAccount.getDayWithdrawalDeposit()))
                            return Mono.error(new CustomException("No se encuentra en el dia para retirar o depositar"));
                    }

                    if (depositDTO.getAmount() <= 0) {
                        return Mono.error(new CustomException("El monto debe ser mayor a 0"));
                    }
                    bankAccount.setBalance(bankAccount.getBalance() + depositDTO.getAmount());

                    return getBankAccountMono(bankAccount, "deposit", depositDTO);
                });
    }

    private Mono<BankAccount> getBankAccountMono(BankAccount bankAccount, String type, DepositDTO depositDTO) {
        List<Movement> getMovements = bankAccount.getMovements();
        if (getMovements == null) {
            getMovements = new ArrayList<>();
        }

        Movement movement = Movement.builder()
                .type(type)
                .date(LocalDateTime.now())
                .amount(depositDTO.getAmount())
                .build();
        getMovements.add(movement);

        // ordenamos los movimientos del ultimo al primero
        getMovements = getMovements.stream()
                .sorted(Comparator.comparing(Movement::getDate).reversed())
                .collect(Collectors.toList());
        bankAccount.setMovements(getMovements);
        bankAccount.setLimitMovements(bankAccount.getLimitMovements()-1);
        return bankAccountRepository.save(bankAccount);
    }


    @Override
    public Mono<BankAccount> withdrawBankAccount(DepositDTO depositDTO) {
        return bankAccountRepository.findById(depositDTO.getId())
                .switchIfEmpty(Mono.error(new CustomException("Cuenta no encontrada")))
                .flatMap(bankAccount -> {
                    if (depositDTO.getAmount() <= 0) {
                        return Mono.error(new CustomException("El monto debe ser mayor a 0"));
                    }

                    if (bankAccount.getLimitMovements() == 0){
                        return Mono.error(new CustomException("Agotaste los movimientos del mes"));
                    }

                    if (depositDTO.getAmount() > bankAccount.getBalance()) {
                        return Mono.error(new CustomException("Sin saldo suficiente"));
                    }

                    bankAccount.setBalance(bankAccount.getBalance() - depositDTO.getAmount());
                    return getBankAccountMono(bankAccount, "withdraw", depositDTO);
                });
    }

    @Override
    public Mono<BankAccount> getBankAccountByProductId(String productId) {
        return bankAccountRepository.findById(productId)
                .switchIfEmpty(Mono.error(new CustomException("No se encontraron elementos")));
    }
}
