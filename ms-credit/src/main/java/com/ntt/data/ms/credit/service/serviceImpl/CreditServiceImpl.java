package com.ntt.data.ms.credit.service.serviceImpl;

import com.ntt.data.ms.credit.client.ClientDTO;
import com.ntt.data.ms.credit.config.CustomException;
import com.ntt.data.ms.credit.dto.PaymentDTO;
import com.ntt.data.ms.credit.dto.SpendDTO;
import com.ntt.data.ms.credit.entity.Charge;
import com.ntt.data.ms.credit.entity.Credit;
import com.ntt.data.ms.credit.entity.CreditType;
import com.ntt.data.ms.credit.entity.Payment;
import com.ntt.data.ms.credit.model.InlineResponse200;
import com.ntt.data.ms.credit.model.ThirdPartyPaymentRequest;
import com.ntt.data.ms.credit.repository.CreditRepository;
import com.ntt.data.ms.credit.service.CreditService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class CreditServiceImpl implements CreditService {
    private final CreditRepository creditRepository;

    private final WebClient bankAccountApiClient;

    private final WebClient customerApiClient;

    public CreditServiceImpl(CreditRepository creditRepository,
                             @Qualifier("bankAccountApiClient") WebClient bankAccountApiClient,
                             @Qualifier("customerApiClient") WebClient customerApiClient) {
        this.creditRepository = creditRepository;
        this.customerApiClient = customerApiClient;
        this.bankAccountApiClient = bankAccountApiClient;
    }

    public Mono<ClientDTO> getDataClient(String id) {
        return customerApiClient.get()
            .uri(uriBuilder -> uriBuilder.path("/customers/{id}").build(id)) // Path variable
            .retrieve()
            .bodyToMono(ClientDTO.class); // Registrar el error;
    }

    public Mono<ClientDTO> fetchBankAccountByClientId(String id) {
        return customerApiClient.get()
            .uri(uriBuilder -> uriBuilder.path("/accounts/bank/{id}/customer")
                .build(id)) // Path variable
            .retrieve()
            .bodyToMono(ClientDTO.class); // Registrar el error;
    }

    public Mono<Error> fallback(Credit credit, Throwable t) {
        return Mono.error(
            new CustomException("El servicio esta caido, por favor intentar en unos momentos"));
    }

    @Override
    public Flux<Credit> getAll() {
        return creditRepository.findAll();
    }

    @Override
    @CircuitBreaker(name = "myService", fallbackMethod = "fallback")
    public Mono<Credit> create(Credit credit) {
        return getDataClient(String.valueOf(credit.getClientId()))
            .switchIfEmpty(Mono.error(new CustomException("El cliente no existe")))
            .flatMap(clientDTO -> {
                try {

                    // Validar si el tipo de crédito coincide
                    // con el tipo de cliente (excepto TARJETA_CREDITO)
                    if (!credit.getType().equals(CreditType.CREDIT_CARD)) {
                        var tipoClient = clientDTO.getType().getCustomerType().name();
                        var tipoRequest = credit.getType().name();

                        if (!tipoClient.equals(tipoRequest))
                            return Mono.error(
                                new CustomException("El tipo de crédito no coincide " +
                                    "con el tipo de cliente"));
                    }


                    credit.setBalance(credit.getBalance());
                    credit.setStatus(true);

                    if (!credit.getType().equals(CreditType.CREDIT_CARD)) {
                        // Setear la tasa de interés
                        credit.setInterestRate();
                        var quota = calculateQuotaMonth(credit.getBalance(),
                            credit.getInterestRate(),
                            credit.getTermMonths());
                        credit.setBalanceWithInterestRate(
                            aroundTwoDecimal(quota * credit.getTermMonths()));
                        credit.setMonthlyFee(quota);
                    }

                    // Si es un crédito PERSONAL, verificamos que el
                    // cliente no tenga ya un crédito activo
                    if (credit.getType().equals(CreditType.PERSONAL)) {
                        return creditRepository.countByClientIdAndType(credit.getClientId(),
                                CreditType.PERSONAL)
                            .flatMap(count -> {
                                if (count > 0) {
                                    return Mono.error(
                                        new CustomException("Un cliente personal solo" +
                                            " puede tener un crédito activo"));
                                }
                                return saveCredit(credit);
                            });
                    }
                    credit.setCreditLimit(credit.getBalance());
                    if (credit.getType().equals(CreditType.CREDIT_CARD)) {
                        credit.setAvailableBalance(credit.getCreditLimit());
                    }

                    // Si es un crédito BUSINESS o TARJETA_CREDITO, lo guardamos directamente
                    return saveCredit(credit);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Mono.error(
                        new CustomException("Ocurrió un error al procesar " +
                            "el crédito: " + e.getMessage()));
                }
            });
    }

    @Override
    public Mono<Credit> paymentCredit(PaymentDTO paymentDTO) {
        return creditRepository.findById(paymentDTO.getIdCard())
            .switchIfEmpty(Mono.error(new CustomException("Cuenta no encontrada")))
            .flatMap(credit -> {
                if (!credit.getStatus())
                    return Mono.error(
                        new CustomException("El credito esta vacio o desactivado"));

                if (paymentDTO.getAmount() <= 0) {
                    return Mono.error(new CustomException("El monto debe ser mayor a 0"));
                }

                List<Payment> payments = credit.getPayments();

                if (payments == null) {
                    payments = new ArrayList<>();
                }

                var paymentsMissing = payments.stream()
                    .mapToDouble(Payment::getAmount)
                    .sum();

                Payment payment = new Payment();
                payment.setAmount(paymentDTO.getAmount());
                payment.setDate(LocalDateTime.now());
                payments.add(payment);

                payments = payments.stream()
                    .sorted(Comparator.comparing(Payment::getDate).reversed())
                    .collect(Collectors.toList());

                var paymentsMissingWithAmount = payments.stream()
                    .mapToDouble(Payment::getAmount)
                    .sum();


                if (credit.getType() == CreditType.CREDIT_CARD) {
                    var needPay = paymentsMissing - credit.getCreditLimit();
                    if (paymentsMissingWithAmount > credit.getCreditLimit()) {
                        return Mono.error(
                            new CustomException("Usted solo necesita pagar: "
                                + aroundTwoDecimal(needPay)));
                    }

                    credit.setAvailableBalance(
                        aroundTwoDecimal(credit.getAvailableBalance() +
                            paymentDTO.getAmount()));

                    if (Objects.equals(0.00, credit.getAvailableBalance())) {
                        credit.setStatus(false);
                    }
                } else {
                    if (paymentsMissingWithAmount > credit.getBalanceWithInterestRate()) {
                        var needPay = credit.getBalanceWithInterestRate() - credit.getBalance();
                        return Mono.error(
                            new CustomException("Usted solo necesita pagar: "
                                + aroundTwoDecimal(needPay)));
                    }
                    credit.setBalance(aroundTwoDecimal(paymentsMissingWithAmount));
                    if (Objects.equals(credit.getBalance(),
                        credit.getBalanceWithInterestRate())) {
                        credit.setStatus(false);
                    }
                }

                credit.setPayments(payments);
                return creditRepository.save(credit);
            });
    }

    @Override
    public Flux<Credit> getCreditByClient(ObjectId clientId) {
        return creditRepository.findByClientId(clientId);
    }

    @Override
    public Mono<Credit> spendCredit(SpendDTO spendDTO) {
        return creditRepository.findById(spendDTO.getIdCard())
            .switchIfEmpty(Mono.error(new CustomException("Cuenta no encontrada")))
            .flatMap(credit -> {
                if (credit.getType() != CreditType.CREDIT_CARD)
                    return Mono.error(
                        new CustomException("No es una tarjeta de crédito"));

                if (!credit.getStatus())
                    return Mono.error(
                        new CustomException("El credito esta vacio o desactivado"));

                if (credit.getAvailableBalance() == 0.00)
                    return Mono.error(
                        new CustomException("Ya no tiene saldo disponible"));

                var sumaGastos = spendDTO.getCharges().stream()
                    .mapToDouble(Charge::getAmount).sum();
                if (sumaGastos <= 0) {
                    return Mono.error(
                        new CustomException("El monto debe ser mayor a 0"));
                }

                if (sumaGastos > credit.getAvailableBalance()) {
                    return Mono.error(
                        new CustomException("Solo le queda disponible: "
                            + credit.getAvailableBalance()));
                }

                credit.setAvailableBalance(
                    aroundTwoDecimal(credit.getAvailableBalance() - sumaGastos));


                List<Charge> charges = credit.getCharges();

                if (charges == null) {
                    charges = new ArrayList<>();
                }

                spendDTO.getCharges().forEach(charge -> {
                    charge.setDate(LocalDateTime.now());
                });

                charges.addAll(spendDTO.getCharges());

                charges = charges.stream()
                    .sorted(Comparator.comparing(Charge::getDate).reversed())
                    .collect(Collectors.toList());

                credit.setCharges(charges);

                return creditRepository.save(credit);
            });
    }

    @Override
    public Mono<Credit> getCreditById(String id) {
        return creditRepository.findById(id);
    }

    @Override
    public Mono<InlineResponse200> payThirdPartyViaDebitCard(
        Mono<ThirdPartyPaymentRequest> thirdPartyPaymentRequest) {
        return thirdPartyPaymentRequest
            .flatMap(request -> creditRepository.findById(request.getIdCardDestiny())
                .switchIfEmpty(Mono.error(new CustomException("Cuenta no encontrada")))
                .flatMap(credit -> {

                    if (!credit.getStatus())
                        return Mono.error(
                            new CustomException("El credito esta vacio o desactivado"));

                    if (request.getAmount() <= 0) {
                        return Mono.error(new CustomException("El monto debe ser mayor a 0"));
                    }

                    List<Payment> payments = credit.getPayments();

                    if (payments == null) {
                        payments = new ArrayList<>();
                    }

                    Payment payment = new Payment();
                    payment.setAmount(request.getAmount());
                    payment.setDate(LocalDateTime.now());
                    payments.add(payment);

                    payments = payments.stream()
                        .sorted(Comparator.comparing(Payment::getDate).reversed())
                        .collect(Collectors.toList());

                    var paymentsMissingWithAmount = payments.stream()
                        .mapToDouble(Payment::getAmount)
                        .sum();

                    if (paymentsMissingWithAmount > credit.getBalanceWithInterestRate()) {
                        var needPay = credit.getBalanceWithInterestRate() - credit.getBalance();
                        return Mono.error(
                            new CustomException("Usted solo necesita pagar: "
                                + aroundTwoDecimal(needPay)));
                    }
                    credit.setBalance(aroundTwoDecimal(paymentsMissingWithAmount));
                    if (Objects.equals(credit.getBalance(),
                        credit.getBalanceWithInterestRate())) {
                        credit.setStatus(false);
                    }

                    credit.setPayments(payments);

                    return creditRepository.save(credit)
                        .map(credit1 -> {
                            InlineResponse200 response = new InlineResponse200();
                            response.setMessage("Pago realizado con éxito");
                            return response;
                        });
                }));
    }

    // Metodo auxiliar para guardar el crédito
    private Mono<Credit> saveCredit(Credit credit) {
        credit.setGrantDate(LocalDateTime.now());
        return creditRepository.save(credit);
    }

    public static double calculateQuotaMonth(double balance,
                                             double interestRateYear,
                                             int months) {
        double interestRateMonth = (interestRateYear / 100) / 12;
        double cuota = (balance * interestRateMonth) /
            (1 - Math.pow(1 + interestRateMonth, -months));
        return aroundTwoDecimal(cuota);
    }

    public static double aroundTwoDecimal(double valor) {
        return new BigDecimal(valor).setScale(2,
            RoundingMode.HALF_UP).doubleValue();
    }


}
