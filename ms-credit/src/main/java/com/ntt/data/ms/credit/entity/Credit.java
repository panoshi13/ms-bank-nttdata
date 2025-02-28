package com.ntt.data.ms.credit.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ntt.data.ms.credit.config.ObjectIdSerializer;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Credits")
public class Credit {
    @Id
    private String id;

    private CreditType type;

    @Field("client_id")
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId clientId;

    @Field("balance")
    private Double balance;

    @Field("interest_rate")
    @Setter(AccessLevel.NONE)
    private Double interestRate;

    private Double balanceWithInterestRate;

    private Double creditLimit;

    private Double availableBalance;

    @Field("monthly_fee")
    private Double monthlyFee;

    @Field("term_months")
    private Integer termMonths;

    private Boolean status;

    @Field("grant_date")
    private LocalDateTime grantDate;

    @Field("payments")
    private List<Payment> payments;

    private List<Charge> charges;

    // Metodo para setear la tasa de interés según el tipo de crédito
    public void setInterestRate() {
        if (CreditType.CREDIT_CARD.equals(this.type)) {
            this.interestRate = 2.5; // Ejemplo para tarjetas de crédito
        } else if (CreditType.BUSINESS.equals(this.type)) {
            this.interestRate = 3.0; // Ejemplo para préstamos personales
        } else if (CreditType.PERSONAL.equals(this.type)) {
            this.interestRate = 1.8; // Ejemplo para préstamos hipotecarios
        } else {
            this.interestRate = 2.0; // Valor por defecto
        }
    }

    // Método estático para crear una lista de objetos Credit con datos hardcoded
    public static List<Credit> getHardcodedCredits() {
        Credit credit1 = new Credit();
        credit1.id = "1";
        credit1.type = CreditType.CREDIT_CARD;
        credit1.clientId = new ObjectId("5f50c31f1c9d440000cf41a2");
        credit1.balance = 5000.0;
        credit1.setInterestRate();
        credit1.balanceWithInterestRate = 5100.0;
        credit1.creditLimit = 10000.0;
        credit1.availableBalance = 5000.0;
        credit1.monthlyFee = 200.0;
        credit1.termMonths = 12;
        credit1.status = true;
        credit1.grantDate = LocalDateTime.of(2022, 1, 1, 0, 0);
        credit1.payments = Arrays.asList(
            new Payment(200.0, LocalDateTime.of(2022, 2, 1, 0, 0)),
            new Payment(200.0, LocalDateTime.of(2022, 3, 1, 0, 0))
        );
        credit1.charges = List.of(
            new Charge("Maintenance Fee", 15.0, LocalDateTime.now())
        );

        Credit credit2 = new Credit();
        credit2.id = "2";
        credit2.type = CreditType.PERSONAL;
        credit2.clientId = new ObjectId("5f50c31f1c9d440000cf41a3");
        credit2.balance = 3000.0;
        credit2.setInterestRate();
        credit2.balanceWithInterestRate = 3054.0;
        credit2.creditLimit = null;
        credit2.availableBalance = null;
        credit2.monthlyFee = 150.0;
        credit2.termMonths = 24;
        credit2.status = false;
        credit2.grantDate = LocalDateTime.of(2021, 6, 15, 0, 0);
        credit2.payments = Arrays.asList(
            new Payment(150.0, LocalDateTime.of(2021, 7, 15, 0, 0)),
            new Payment(150.0, LocalDateTime.of(2021, 8, 15, 0, 0))
        );
        credit2.charges = List.of(
            new Charge("Early Termination Fee", 25.0, LocalDateTime.now())
        );

        return Arrays.asList(credit1, credit2);
    }
}
