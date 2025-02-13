package com.ntt.data.ms.credit.serviceImpl;

import com.ntt.data.ms.credit.client.ClientDTO;
import com.ntt.data.ms.credit.config.CustomException;
import com.ntt.data.ms.credit.dto.PaymentDTO;
import com.ntt.data.ms.credit.entity.Credit;
import com.ntt.data.ms.credit.entity.CreditCard;
import com.ntt.data.ms.credit.entity.CreditType;
import com.ntt.data.ms.credit.entity.Payment;
import com.ntt.data.ms.credit.repository.CreditRepository;
import com.ntt.data.ms.credit.service.CreditService;
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
    private final WebClient webClient;

    public CreditServiceImpl(CreditRepository creditRepository, WebClient.Builder webClientBuilder) {
        this.creditRepository = creditRepository;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8085").build();
    }

    @Override
    public Flux<Credit> getAll() {
        return null;
    }

    @Override
    public Mono<Credit> create(Credit credit) {
        return getDataClient(String.valueOf(credit.getClientId()))
                .switchIfEmpty(Mono.error(new CustomException("El cliente no existe")))
                .flatMap(clientDTO -> {
                    // Validar si el tipo de crédito coincide con el tipo de cliente (excepto TARJETA_CREDITO)
                    if (!credit.getType().equals(CreditType.CREDIT_CARD) &&
                            !credit.getType().name().equals(clientDTO.getType().name())) {
                        return Mono.error(new CustomException("El tipo de crédito no coincide con el tipo de cliente"));
                    }

                    credit.setBalance(0.00);
                    credit.setStatus(true);

                    if (!credit.getType().equals(CreditType.CREDIT_CARD)) {
                        // Setear la tasa de interés
                        credit.setInterestRate();
                        var quota = calculateQuotaMonth(credit.getAmount(), credit.getInterestRate(), credit.getTermMonths());
                        credit.setBalanceWithInterestRate(aroundTwoDecimal(quota * credit.getTermMonths()));
                        credit.setMonthlyFee(quota);
                    }

                    // Si es un crédito PERSONAL, verificamos que el cliente no tenga ya un crédito activo
                    if (credit.getType().equals(CreditType.PERSONAL)) {
                        return creditRepository.countByClientIdAndType(credit.getClientId(), CreditType.PERSONAL)
                                .flatMap(count -> {
                                    if (count > 0) {
                                        return Mono.error(new CustomException("Un cliente personal solo puede tener un crédito activo"));
                                    }
                                    return saveCredit(credit);
                                });
                    }

                    if (credit.getType().equals(CreditType.CREDIT_CARD)) {
                        credit.setCreditLimit(credit.getAmount());
                        credit.setAmount(null);
                        credit.setAvailableBalance(credit.getCreditLimit());
                    }

                    // Si es un crédito BUSINESS o TARJETA_CREDITO, lo guardamos directamente
                    return saveCredit(credit);
                });
    }

    @Override
    public Mono<Credit> paymentCredit(PaymentDTO paymentDTO) {
        return creditRepository.findById(paymentDTO.getId())
                .switchIfEmpty(Mono.error(new CustomException("Cuenta no encontrada")))
                .flatMap(credit -> {
                    if (!credit.getStatus())
                        return Mono.error(new CustomException("El credito esta desactivado"));

                    if (paymentDTO.getAmount() <= 0) {
                        return Mono.error(new CustomException("El monto debe ser mayor a 0"));
                    }

                    List<Payment> payments = credit.getPayments();

                    if (payments == null) {
                        payments = new ArrayList<>();
                    }

                    Payment payment = new Payment();
                    payment.setAmount(paymentDTO.getAmount());
                    payment.setDate(LocalDateTime.now());
                    payments.add(payment);

                    payments = payments.stream()
                            .sorted(Comparator.comparing(Payment::getDate).reversed())
                            .collect(Collectors.toList());

                    var paymentsMissing = payments.stream()
                            .mapToDouble(Payment::getAmount)
                            .sum();

                    if (paymentsMissing > credit.getBalanceWithInterestRate()) {
                        var needPay = credit.getBalanceWithInterestRate() - credit.getBalance();
                        return Mono.error(new CustomException("Usted solo necesita pagar: " + needPay));
                    }

                    credit.setBalance(aroundTwoDecimal(paymentsMissing));
                    if (Objects.equals(credit.getBalance(), credit.getBalanceWithInterestRate())) {
                        credit.setStatus(false);
                    }
                    credit.setPayments(payments);
                    return creditRepository.save(credit);
                });
    }

    // Metodo auxiliar para guardar el crédito
    private Mono<Credit> saveCredit(Credit credit) {
        credit.setGrantDate(LocalDateTime.now());
        return creditRepository.save(credit);
    }

    public static double calculateQuotaMonth(double amount, double interestRateYear, int months) {
        double interestRateMonth = (interestRateYear / 100) / 12;
        double cuota = (amount * interestRateMonth) /
                (1 - Math.pow(1 + interestRateMonth, -months));
        return aroundTwoDecimal(cuota);
    }

    public static double aroundTwoDecimal(double valor) {
        return new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public Mono<ClientDTO> getDataClient(String id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/customers/{id}").build(id)) // Path variable
                .retrieve()
                .bodyToMono(ClientDTO.class);
    }
}
