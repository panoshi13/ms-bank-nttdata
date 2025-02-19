package com.ntt.data.ms.bank.accounts.service.serviceImpl;

import com.ntt.data.ms.bank.accounts.client.dto.ClientDTO;
import com.ntt.data.ms.bank.accounts.client.dto.ClientType;
import com.ntt.data.ms.bank.accounts.client.dto.CreditDTO;
import com.ntt.data.ms.bank.accounts.client.dto.ProfileType;
import com.ntt.data.ms.bank.accounts.config.CustomException;
import com.ntt.data.ms.bank.accounts.dto.DepositDTO;
import com.ntt.data.ms.bank.accounts.entity.AccountType;
import com.ntt.data.ms.bank.accounts.entity.BankAccount;
import com.ntt.data.ms.bank.accounts.entity.Holder;
import com.ntt.data.ms.bank.accounts.entity.Movement;
import com.ntt.data.ms.bank.accounts.model.*;
import com.ntt.data.ms.bank.accounts.repository.BankAccountRepository;
import com.ntt.data.ms.bank.accounts.service.BankAccountService;
import com.ntt.data.ms.bank.accounts.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BankAccountServiceImpl implements BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final WebClient creditApiClient;
    private final WebClient customerApiClient;


    public BankAccountServiceImpl(BankAccountRepository bankAccountRepository,
                                  WebClient.Builder webClientBuilder,
                                  @Qualifier("customerApiClient") WebClient customerApiClient,
                                  @Qualifier("creditApiClient") WebClient creditApiClient) {
        this.bankAccountRepository = bankAccountRepository;
        this.creditApiClient = creditApiClient;
        this.customerApiClient = customerApiClient;
    }

    @Override
    public Flux<BankAccount> getAll() {
        return bankAccountRepository.findAll();
    }

    @Override
    public Flux<BankAccount> getBankAccountsByClientId(String clientId) {
        return bankAccountRepository.findByClientId(new ObjectId(clientId));
    }

    public Mono<ClientDTO> getData(String id) {
        return customerApiClient.get()
                .uri(uriBuilder -> uriBuilder.path("/customers/{id}").build(id)) // Path variable
                .retrieve()
                .bodyToMono(ClientDTO.class);
    }

    @Override
    public Mono<BankAccount> create(BankAccount bankAccount) {
        return getData(String.valueOf(bankAccount.getClientId()))
                .switchIfEmpty(Mono.error(new CustomException("El cliente no existe")))
                .flatMap(client -> validateClientType(client, bankAccount))
                .flatMap(clientType -> validateClientAccounts(bankAccount, clientType))
                .flatMap(this::configureAccount)
                .flatMap(bankAccountRepository::save);
    }

    public Mono<List<CreditDTO>> fetchCreditByClientId(String productId) {
        return creditApiClient.get()
                .uri("/credits/{id}/client", productId)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == 400,
                        clientResponse -> Mono.error(new RuntimeException("Error 400 en consulta de cliente en cuentas de créditos")))
                .bodyToFlux(CreditDTO.class)
                .collectList();
    }

    // Metodo para validar el tipo de cliente
    private Mono<ClientType> validateClientType(ClientDTO client, BankAccount bankAccount) {
        try {
            ClientType type = ClientType.valueOf(client.getType().getCustomerType().toString());
            if (ClientType.PERSONAL.equals(type)) {
                if (AccountType.SAVINGS.equals(bankAccount.getType())) {
                    if (ProfileType.VIP.equals(client.getType().getProfile())) {
                        return fetchCreditByClientId(String.valueOf(client.getId()))
                                .flatMap(creditDTOList -> {
                                    if (creditDTOList.isEmpty()) {
                                        return Mono.error(new CustomException("No tiene una tarjeta de credito para crear su cuenta corriente"));
                                    }
                                    // Imprimir el resultado en la consola
                                    System.out.println("Información de crédito: " + creditDTOList);
                                    return Mono.just(type);
                                });
                    }
                }
            }

            if (ClientType.BUSINESS.equals(type)) {
                if (AccountType.CURRENT.equals(bankAccount.getType())) {
                    if (ProfileType.PYME.equals(client.getType().getProfile())) {
                        return fetchCreditByClientId(String.valueOf(client.getId()))
                                .flatMap(creditDTOList -> {
                                    if (creditDTOList.isEmpty()) {
                                        return Mono.error(new CustomException("No tiene una tarjeta de credito para crear su cuenta corriente"));
                                    }
                                    return Mono.just(type);
                                });
                    }
                }

                if (AccountType.SAVINGS.equals(bankAccount.getType()) || AccountType.FIXED_TERM.equals(bankAccount.getType())) {
                    return Mono.error(new CustomException("No puede tener este tipo de cuenta."));
                }

                if (bankAccount.getHolders() == null || bankAccount.getHolders().isEmpty()) {
                    return Mono.error(new CustomException("Por favor asignar uno o más titulares a su cuenta bancaria."));
                }
                return Mono.just(type);
            }
            bankAccount.setHolders(null);
            bankAccount.setAuthorizedSignatories(null);
            return Mono.just(type);
        } catch (IllegalArgumentException e) {
            return Mono.error(new CustomException("Tipo de cliente no válido: " + client.getType().getCustomerType()));
        }
    }

    //  Metodo para validar si el cliente puede tener la cuenta solicitada
    private Mono<BankAccount> validateClientAccounts(BankAccount bankAccount, ClientType clientType) {
        return bankAccountRepository.findByClientId(bankAccount.getClientId())
                .collectList()
                .flatMap(accounts -> {
                    if (accounts.isEmpty()) {
                        return Mono.just(bankAccount);
                    }

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
        // boolean hasFixedAccount = accounts.stream().anyMatch(acc -> acc.getType() == AccountType.FIXED_TERM);

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
        bankAccount.setBalance(bankAccount.getBalance() != null ? bankAccount.getBalance() : 0.00);

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
    public Mono<BankAccount> depositBankAccount(TransactionRequest depositDTO) {
        return bankAccountRepository.findById(depositDTO.getCardId())
                .switchIfEmpty(Mono.error(new CustomException("Cuenta no encontrada")))
                .flatMap(bankAccount -> {
                    try {
                        boolean existLimit = false;
                        // comisión
                        double comision = depositDTO.getAmount() * 0.05;

                        if ((bankAccount.getType().equals(AccountType.FIXED_TERM) || bankAccount.getType().equals(AccountType.SAVINGS)) && bankAccount.getLimitMovements() <= 0) {
                            // Aplicar la comisión al balance de la cuenta bancaria
                            if (bankAccount.getBalance() - comision < 0.00) {
                                return Mono.error(new CustomException("No cuenta con fondos suficiente por la comision agregada"));
                            }
                            existLimit = true;
                        }

                        if (bankAccount.getType() == AccountType.FIXED_TERM) {
                            if (!Util.isDayOfMoth(bankAccount.getDayWithdrawalDeposit())) {
                                return Mono.error(new CustomException("No se encuentra en el día para retirar o depositar"));
                            }
                        }

                        if (depositDTO.getAmount() <= 0) {
                            return Mono.error(new CustomException("El monto debe ser mayor a 0"));
                        }

                        bankAccount.setBalance(existLimit ? (bankAccount.getBalance() + depositDTO.getAmount()) - comision : bankAccount.getBalance() + depositDTO.getAmount());
                        return getBankAccountMono(bankAccount, "deposit [+]", depositDTO);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        return Mono.error(new CustomException("Ocurrió un error al procesar el depósito"));
                    }
                });
    }

    private Mono<BankAccount> getBankAccountMono(BankAccount bankAccount, String type, TransactionRequest depositDTO) {
        if (bankAccount.getLimitMovements() <= 0) {
            type = type.concat(" - commission: 10.00");
        }

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
        if ((bankAccount.getType().equals(AccountType.FIXED_TERM) || bankAccount.getType().equals(AccountType.SAVINGS)))
            bankAccount.setLimitMovements(bankAccount.getLimitMovements() - 1);
        return bankAccountRepository.save(bankAccount);
    }


    @Override
    public Mono<BankAccount> withdrawBankAccount(TransactionRequest depositDTO) {
        return bankAccountRepository.findById(depositDTO.getCardId())
                .switchIfEmpty(Mono.error(new CustomException("Cuenta no encontrada")))
                .flatMap(bankAccount -> {
                    boolean existLimit = false;
                    // Calcular la comisión como el 5% del monto de la transacción
                    double comision = depositDTO.getAmount() * 0.05;

                    if (depositDTO.getAmount() <= 0) {
                        return Mono.error(new CustomException("El monto debe ser mayor a 0"));
                    }

                    if ((bankAccount.getType().equals(AccountType.FIXED_TERM) || bankAccount.getType().equals(AccountType.SAVINGS)) && bankAccount.getLimitMovements() <= 0) {
                        // Aplicar la comisión al balance de la cuenta bancaria
                        if (depositDTO.getAmount() + comision > bankAccount.getBalance()) {
                            return Mono.error(new CustomException("No cuenta con fondos suficiente por la comisión agregada"));
                        }
                        existLimit = true;
                    }

                    if (depositDTO.getAmount() > bankAccount.getBalance()) {
                        return Mono.error(new CustomException("Sin saldo suficiente"));
                    }

                    bankAccount.setBalance(existLimit ? (bankAccount.getBalance() - depositDTO.getAmount()) - comision : bankAccount.getBalance() - depositDTO.getAmount());
                    return getBankAccountMono(bankAccount, "withdraw [-]", depositDTO);
                });
    }

    @Override
    public Mono<BankAccount> getBankAccountByProductId(String productId) {
        return bankAccountRepository.findById(productId)
                .switchIfEmpty(Mono.error(new CustomException("No se encontraron elementos")));
    }

    @Override
    public Mono<InlineResponse200> transferBetweenAccounts(Mono<TransferRequest> requestMono) {
        return requestMono
                .flatMap(transferRequest -> {
                    // Validar campos
                    if (transferRequest.getSourceAccountId() == null) {
                        return Mono.error(new CustomException("sourceAccountId no puede ser nulo"));
                    }
                    if (transferRequest.getDestinationAccountId() == null) {
                        return Mono.error(new CustomException("destinationAccountId no puede ser nulo"));
                    }
                    if (transferRequest.getAmount() == null) {
                        return Mono.error(new CustomException("amount no puede ser nulo"));
                    }

                    // Buscar cuentas
                    Mono<BankAccount> accountBankSource = bankAccountRepository.findById(transferRequest.getSourceAccountId())
                            .switchIfEmpty(Mono.error(new CustomException("No existe la cuenta de origen")));
                    Mono<BankAccount> accountBankDestini = bankAccountRepository.findById(transferRequest.getDestinationAccountId())
                            .switchIfEmpty(Mono.error(new CustomException("No existe la cuenta de destino")));

                    // Realizar transferencia
                    return Mono.zip(accountBankSource, accountBankDestini)
                            .flatMap(tuple -> {
                                BankAccount sourceAccount = tuple.getT1();
                                BankAccount destinationAccount = tuple.getT2();
                                // Lógica de transferencia (ejemplo simple)
                                if (sourceAccount.getBalance() < transferRequest.getAmount()) {
                                    return Mono.error(new CustomException("Saldo insuficiente en la cuenta de origen"));
                                }

                                // --- movement source transfer
                                List<Movement> getMovementsSource = sourceAccount.getMovements();
                                if (getMovementsSource == null) {
                                    getMovementsSource = new ArrayList<>();
                                }
                                Movement movementSource = Movement.builder()
                                        .amount(transferRequest.getAmount())
                                        .date(LocalDateTime.now())
                                        .type("Transfer [-] Account: " + destinationAccount.getId())
                                        .build();
                                getMovementsSource.add(movementSource);
                                // ordenamos los movimientos del ultimo al primero
                                getMovementsSource = getMovementsSource.stream()
                                        .sorted(Comparator.comparing(Movement::getDate).reversed())
                                        .collect(Collectors.toList());

                                sourceAccount.setMovements(getMovementsSource);
                                sourceAccount.setBalance(sourceAccount.getBalance() - transferRequest.getAmount());

                                // --- movement destini transfer
                                List<Movement> getMovementsDestini = destinationAccount.getMovements();
                                if (getMovementsDestini == null) {
                                    getMovementsDestini = new ArrayList<>();
                                }
                                Movement movementDestini = Movement.builder()
                                        .amount(transferRequest.getAmount())
                                        .date(LocalDateTime.now())
                                        .type("Transfer [+] Account: " + destinationAccount.getId())
                                        .build();
                                getMovementsDestini.add(movementDestini);
                                // ordenamos los movimientos del ultimo al primero
                                getMovementsDestini = getMovementsDestini.stream()
                                        .sorted(Comparator.comparing(Movement::getDate).reversed())
                                        .collect(Collectors.toList());

                                destinationAccount.setMovements(getMovementsDestini);
                                destinationAccount.setBalance(destinationAccount.getBalance() + transferRequest.getAmount());
                                return Mono.when(
                                        bankAccountRepository.save(sourceAccount),
                                        bankAccountRepository.save(destinationAccount)
                                ).thenReturn(new InlineResponse200().message("Transferencia exitosa"));
                            });
                });
    }

    @Override
    public Mono<ReportCommissionResponse> getCommissionReport(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate startDateLocal = LocalDate.parse(startDate, formatter);
        LocalDate endDateLocal = LocalDate.parse(endDate, formatter);

        LocalDateTime start = startDateLocal.atStartOfDay();
        LocalDateTime end = endDateLocal.atStartOfDay();

        return bankAccountRepository.findAll().collectList()
                .flatMap(bankAccounts -> {
                    ReportCommissionResponse reportCommissionResponse = new ReportCommissionResponse();
                    reportCommissionResponse.startDate(startDate);
                    reportCommissionResponse.endDate(endDate);

                    Map<String, Double> commissionByProductMap = getCommissionByProductMap(bankAccounts, start, end);

                    List<ReportCommissionResponseCommissionByProduct> commissionByProducts = commissionByProductMap.entrySet().stream()
                            .map(entry -> createCommissionByProduct(entry))
                            .collect(Collectors.toList());

                    reportCommissionResponse.commissionByProduct(commissionByProducts);
                    return Mono.just(reportCommissionResponse);
                });
    }

    private Map<String, Double> getCommissionByProductMap(List<BankAccount> bankAccounts, LocalDateTime start, LocalDateTime end) {
        return bankAccounts.stream()
                .filter(bankAccount -> bankAccount.getMovements() != null && hasCommissionMovementsInDateRange(bankAccount, start, end))
                .collect(Collectors.groupingBy(
                        bankAccount -> bankAccount.getType().name(),
                        Collectors.summingDouble(bankAccount -> bankAccount.getMovements().stream()
                                .filter(movement -> hasCommissionType(movement.getType()))
                                .mapToDouble(movement -> parseCommission(movement.getType()))
                                .sum()
                        )
                ));
    }

    private boolean hasCommissionType(String type) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(type);
        return matcher.find();
    }

    private double parseCommission(String type) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(type);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        } else {
            return 0.0;
        }
    }

    private ReportCommissionResponseCommissionByProduct createCommissionByProduct(Map.Entry<String, Double> entry) {
        ReportCommissionResponseCommissionByProduct report = new ReportCommissionResponseCommissionByProduct();
        report.setTypeProduct(entry.getKey());
        report.setTotalCommission(entry.getValue());
        return report;
    }

    private boolean hasCommissionMovementsInDateRange(BankAccount bankAccount, LocalDateTime start, LocalDateTime end) {
        return bankAccount.getMovements().stream()
                .anyMatch(movement -> {
                    LocalDateTime movementDate = movement.getDate();
                    return hasCommissionType(movement.getType()) && !movementDate.toLocalDate().isBefore(start.toLocalDate()) && !movementDate.toLocalDate().isAfter(end.toLocalDate());
                });
    }

}
