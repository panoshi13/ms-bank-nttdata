package com.ntt.data.ms.client.service.serviceImpl;

import com.ntt.data.ms.client.client.BankAccountDTO;
import com.ntt.data.ms.client.client.CreditDTO;
import com.ntt.data.ms.client.config.CustomException;
import com.ntt.data.ms.client.dto.BalanceAvailableDTO;
import com.ntt.data.ms.client.entity.Customer;
import com.ntt.data.ms.client.mapper.ClientMapper;
import com.ntt.data.ms.client.model.*;
import com.ntt.data.ms.client.repository.CustomerRepository;
import com.ntt.data.ms.client.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final WebClient bankAccountApiClient;
    private final WebClient creditApiClient;
    private final ClientMapper clientMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               @Qualifier("bankAccountApiClient") WebClient bankAccountApiClient,
                               @Qualifier("creditApiClient") WebClient creditApiClient,
                               ClientMapper clientMapper) {
        this.customerRepository = customerRepository;
        this.bankAccountApiClient = bankAccountApiClient;
        this.creditApiClient = creditApiClient;
        this.clientMapper = clientMapper;
    }

    @Override
    public Mono<Customer> create(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Mono<Customer> findById(String id) {
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException("No se encontraron resultados")));
    }

    public Mono<CreditDTO> fetchCredit(String productId) {
        return creditApiClient.get()
                .uri("/credits/{id}", productId)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == 400,
                        clientResponse -> Mono.error(new RuntimeException("Error 400 en consulta de movimientos de tarjeta")))
                .bodyToMono(CreditDTO.class);
    }

    public Mono<BankAccountDTO> fetchBankAccount(String productId) {
        return bankAccountApiClient.get()
                .uri("/accounts/bank/{id}", productId)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == 400,
                        clientResponse -> Mono.error(new CustomException("Error 400 en consulta de movimientos de tarjeta")))
                .bodyToMono(BankAccountDTO.class);
    }

    public Flux<BankAccountDTO> fetchBankAccountByClientId(String clientId) {
        return bankAccountApiClient.get()
                .uri("/accounts/bank/{id}/customer", clientId)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == 400,
                        clientResponse -> Mono.error(new CustomException("Error 400 en consulta de la cuenta bancaria por cliente")))
                .bodyToFlux(BankAccountDTO.class);
    }

    public Flux<CreditDTO> fetchCreditByClientId(String clientId) {
        return creditApiClient.get()
                .uri("/credits/{id}/client", clientId)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == 400,
                        clientResponse -> Mono.error(new CustomException("Error 400 en consulta del credito por cliente")))
                .bodyToFlux(CreditDTO.class);
    }


    @Override
    public Flux<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Mono<Customer> update(Customer customer) {
        // Asegurar que el _id sea un ObjectId
        if (customer.getId() == null) {
            return Mono.error(new CustomException("El id no puede ser nulo"));
        }

        return customerRepository.findById(String.valueOf(customer.getId()))
                .flatMap(existingCliente -> {
                    customer.setId(existingCliente.getId()); // Asegurar que se usa el mismo ID
                    return customerRepository.save(customer);
                })
                .switchIfEmpty(Mono.error(new CustomException("Cliente no encontrado")));
    }

    @Override
    public Mono<String> delete(String id) {
        return customerRepository.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return customerRepository.deleteById(id)
                                .thenReturn("Cliente eliminado con éxito");
                    } else {
                        return Mono.just("Cliente no encontrado");
                    }
                });
    }

    @Override
    public Mono<CustomerProductMovementsResponse> getMovement(String clientId, String productId) {
        return customerRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new RuntimeException("Cliente no encontrado")))
                .flatMap(customer -> getProductDTOMono(productId, customer));

    }

    @Override
    public Mono<CustomerProductBalanceResponse> getBalanceAvailable(String clientId, String productId) {
        return customerRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new RuntimeException("Cliente no encontrado")))
                .flatMap(customer -> getCustomerProductBalanceResponse(productId, customer));
    }

    @Override
    public Mono<MonthlyBalanceReportResponse> getMonthlyBalanceReport(String clientId) {
        Flux<BankAccountDTO> bankAccountDTOFlux = fetchBankAccountByClientId(clientId)
                .onErrorResume(throwable -> {
                    log.error("Error al obtener saldo de cuenta bancaria: {}", throwable.getMessage());
                    return Flux.empty();
                });
        Flux<CreditDTO> creditDTOFlux = fetchCreditByClientId(clientId)
                .onErrorResume(throwable -> {
                    log.error("Error al obtener saldo de cuenta crédito: {}", throwable.getMessage());
                    return Flux.empty();
                });

        return Mono.zip(
                        bankAccountDTOFlux.collectList(),
                        creditDTOFlux.collectList()
                )
                .flatMap(tuple -> {
                    List<BankAccountDTO> bankAccounts = tuple.getT1();
                    List<CreditDTO> credits = tuple.getT2();

                    // Calcular saldos promedio diarios
                    List<MonthlyBalanceReportResponseAverageBalancesBankAccounts> balancesBankAccountsProducts = bankAccounts.stream()
                            .map(account -> {
                                MonthlyBalanceReportResponseAverageBalancesBankAccounts balancesBankAccounts = new MonthlyBalanceReportResponseAverageBalancesBankAccounts();
                                LocalDate today = LocalDate.now();
                                int dayOfMonth = today.getDayOfMonth();
                                balancesBankAccounts.setAccountId(account.getId());
                                var balanceAverage = account.getBalance() / dayOfMonth;
                                balancesBankAccounts.setAverageDailyBalance(aroundTwoDecimal(balanceAverage));
                                return balancesBankAccounts;
                            })
                            .collect(Collectors.toList());

                    // Calcular saldos promedio diarios
                    List<MonthlyBalanceReportResponseAverageBalancesCreditProducts> balancesCreditProducts  = credits.stream()
                            .map(credit -> {
                                MonthlyBalanceReportResponseAverageBalancesCreditProducts creditProducts = new MonthlyBalanceReportResponseAverageBalancesCreditProducts();
                                LocalDate today = LocalDate.now();
                                int dayOfMonth = today.getDayOfMonth();
                                creditProducts.setProductId(credit.getId());
                                var balanceAverage = credit.getAvailableBalance() / dayOfMonth;
                                creditProducts.setAverageDailyBalance(aroundTwoDecimal(balanceAverage));
                                return creditProducts;
                            })
                            .collect(Collectors.toList());

                    // Crear el reporte
                    MonthlyBalanceReportResponse report = new MonthlyBalanceReportResponse();
                    report.setClientId(clientId);

                    MonthlyBalanceReportResponseAverageBalances averageBalances = new MonthlyBalanceReportResponseAverageBalances();
                    averageBalances.setBankAccounts(balancesBankAccountsProducts);
                    averageBalances.setCreditProducts(balancesCreditProducts);

                    report.averageBalances(averageBalances);
                    return Mono.just(report);
                });
    }

    public static double aroundTwoDecimal(double valor) {
        return new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }


    private Mono<CustomerProductBalanceResponse> getCustomerProductBalanceResponse(String productId, Customer customer) {
        Mono<BankAccountDTO> bankAccountDTOMono = fetchBankAccount(productId)
                .onErrorResume(throwable -> {
                    log.error("Error al obtener saldo de cuenta bancaria: {}", throwable.getMessage());
                    return Mono.empty();
                });
        Mono<CreditDTO> creditDTOMono = fetchCredit(productId)
                .onErrorResume(throwable -> {
                    log.error("Error al obtener saldo de cuenta de crédito: {}", throwable.getMessage());
                    return Mono.empty();
                });

        return Mono.zip(
                        bankAccountDTOMono.hasElement(),
                        creditDTOMono.hasElement()
                )
                .flatMap(hasElements -> {
                    boolean hasBankAccount = hasElements.getT1();
                    boolean hasCreditAccount = hasElements.getT2();
                    if (!hasBankAccount && !hasCreditAccount) {
                        return Mono.error(new CustomException("No se pudo obtener el saldo"));
                    }
                    return Mono.zip(
                                    bankAccountDTOMono.defaultIfEmpty(new BankAccountDTO()),
                                    creditDTOMono.defaultIfEmpty(new CreditDTO())
                            )
                            .map(tuple -> getBalanceResponse(customer, tuple));
                });
    }

    private static CustomerProductBalanceResponse getBalanceResponse(Customer customer, Tuple2<BankAccountDTO, CreditDTO> tuple) {
        BankAccountDTO bankAccountDTO = tuple.getT1();
        CreditDTO creditDTO = tuple.getT2();
        CustomerProductBalanceResponse balanceResponse = new CustomerProductBalanceResponse();
        balanceResponse.name(customer.getName());
        balanceResponse.identification(customer.getIdentification());
        balanceResponse.typeCard(bankAccountDTO.getType() == null ? creditDTO.getType().name() : bankAccountDTO.getType().name());
        balanceResponse.availableBalance(bankAccountDTO.getType() == null ? creditDTO.getAvailableBalance() : bankAccountDTO.getBalance());
        return balanceResponse;
    }

    private Mono<CustomerProductMovementsResponse> getProductDTOMono(String productId, Customer customer) {
        Mono<BankAccountDTO> bankAccountDTOMono = fetchBankAccount(productId)
                .onErrorResume(throwable -> {
                    log.error("Error al obtener movimientos de cuenta bancaria: {}", throwable.getMessage());
                    return Mono.empty();
                });
        Mono<CreditDTO> creditDTOMono = fetchCredit(productId)
                .onErrorResume(throwable -> {
                    log.error("Error al obtener movimientos de cuenta de crédito: {}", throwable.getMessage());
                    return Mono.empty();
                });

        return Mono.zip(
                        bankAccountDTOMono.hasElement(),
                        creditDTOMono.hasElement()
                )
                .flatMap(hasElements -> {
                    boolean hasBankAccount = hasElements.getT1();
                    boolean hasCreditAccount = hasElements.getT2();
                    if (!hasBankAccount && !hasCreditAccount) {
                        return Mono.error(new CustomException("No se pudieron obtener los movimientos"));
                    }
                    return Mono.zip(
                                    bankAccountDTOMono.defaultIfEmpty(new BankAccountDTO()),
                                    creditDTOMono.defaultIfEmpty(new CreditDTO())
                            )
                            .map(tuple -> getMovementsResponse(customer, tuple));
                });
    }

    private CustomerProductMovementsResponse getMovementsResponse(Customer customer, Tuple2<BankAccountDTO, CreditDTO> tuple) {
        BankAccountDTO bankAccountDTO = tuple.getT1();
        CreditDTO creditDTO = tuple.getT2();
        CustomerProductMovementsResponse movementsResponse = new CustomerProductMovementsResponse();
        movementsResponse.setName(customer.getName());
        movementsResponse.setIdentification(customer.getIdentification());
        movementsResponse.setMovementsBankAccount(clientMapper.movementsBankAccounts(bankAccountDTO.getMovements()));
        movementsResponse.setMovementsCredit(clientMapper.movementsCredit(creditDTO));
        return movementsResponse;
    }


}
