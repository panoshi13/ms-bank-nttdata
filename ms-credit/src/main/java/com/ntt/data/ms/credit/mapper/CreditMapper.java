package com.ntt.data.ms.credit.mapper;


import com.ntt.data.ms.credit.dto.PaymentDTO;
import com.ntt.data.ms.credit.dto.SpendDTO;
import com.ntt.data.ms.credit.entity.Charge;
import com.ntt.data.ms.credit.entity.Credit;
import com.ntt.data.ms.credit.entity.CreditType;
import com.ntt.data.ms.credit.entity.Payment;
import com.ntt.data.ms.credit.model.*;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreditMapper {
    public Credit mapToCredit(CreditRequest creditRequest) {
        var credit = new Credit();
        credit.setType(CreditType.valueOf(creditRequest.getType()));
        credit.setClientId(new ObjectId(creditRequest.getClientId()));
        credit.setBalance(creditRequest.getBalance());
        credit.setTermMonths(creditRequest.getTermMonths());
        return credit;
    }

    public PaymentDTO mapToPaymentDTO(PaymentRequest paymentRequest) {
        var paymentDTO = new PaymentDTO();
        paymentDTO.setIdCard(paymentRequest.getIdCard());
        paymentDTO.setAmount(paymentRequest.getAmount());
        return paymentDTO;
    }

    public SpendDTO mapToSpendDTO(SpendRequest spendRequest) {
        var spendDTO = new SpendDTO();
        spendDTO.setIdCard(spendRequest.getIdCard());
        List<Charge> charges = spendRequest.getCharges().stream()
                .map(spendRequestCharges -> {
                    return new Charge(
                            spendRequestCharges.getDescription(),
                            spendRequestCharges.getAmount(),
                            LocalDateTime.now()
                    );
                }).toList();
        spendDTO.setCharges(charges);
        return spendDTO;
    }

    public CreditResponse mapToCreditResponse(Credit credit) {
        CreditResponse creditResponse = getCreditResponse(credit);
        creditResponse.grantDate(credit.getGrantDate().atOffset(ZoneOffset.UTC));

        List<Payment> paymentList = credit.getPayments();
        List<Charge> chargeList = credit.getCharges();

        List<CreditResponsePayments> payments = null;
        if (paymentList != null) {
            payments = credit.getPayments().stream()
                    .map(payment -> new CreditResponsePayments()
                            .amount(payment.getAmount())
                            .date(payment.getDate().atOffset(ZoneOffset.UTC)))
                    .collect(Collectors.toList());
        }
        creditResponse.payments(payments);
        List<CreditResponseCharges> charges = null;
        if (chargeList != null) {
            charges = credit.getCharges().stream()
                    .map(charge -> new CreditResponseCharges()
                            .description(charge.getDescription())
                            .amount(charge.getAmount())
                            .date(charge.getDate().atOffset(ZoneOffset.UTC)))
                    .collect(Collectors.toList());
            creditResponse.charges(charges);

        }
        creditResponse.setCharges(charges);
        return creditResponse;
    }


    public Flux<CreditResponse> mapToCreditResponseFlux(Flux<Credit> creditFlux) {
        return creditFlux.map(credit -> {
            var creditResponse = getCreditResponse(credit);
            List<Payment> paymentList = credit.getPayments();
            List<Charge> chargeList = credit.getCharges();

            List<CreditResponsePayments> payments = null;
            if (paymentList != null) {
                payments = credit.getPayments().stream()
                        .map(payment -> new CreditResponsePayments()
                                .amount(payment.getAmount())
                                .date(payment.getDate().atOffset(ZoneOffset.UTC)))
                        .collect(Collectors.toList());
            }
            creditResponse.payments(payments);

            List<CreditResponseCharges> charges = null;
            if (chargeList != null) {
                charges = credit.getCharges().stream()
                        .map(charge -> new CreditResponseCharges()
                                .description(charge.getDescription())
                                .amount(charge.getAmount())
                                .date(charge.getDate().atOffset(ZoneOffset.UTC)))
                        .collect(Collectors.toList());
            }
            creditResponse.charges(charges);
            return creditResponse;
        });

    }


    private CreditResponse getCreditResponse(Credit credit) {
        CreditResponse creditResponse = new CreditResponse();
        creditResponse.id(credit.getId());
        creditResponse.type(credit.getType().name());
        creditResponse.balance(creditResponse.getBalance());
        creditResponse.clientId(String.valueOf(credit.getClientId()));
        creditResponse.creditLimit(credit.getCreditLimit());
        creditResponse.interestRate(credit.getInterestRate());
        creditResponse.balance(credit.getBalance());
        creditResponse.availableBalance(credit.getAvailableBalance());
        creditResponse.balanceWithInterestRate(credit.getBalanceWithInterestRate());
        creditResponse.monthlyFee(credit.getMonthlyFee());
        creditResponse.termMonths(credit.getTermMonths());
        creditResponse.status(credit.getStatus());
        return creditResponse;
    }
}
