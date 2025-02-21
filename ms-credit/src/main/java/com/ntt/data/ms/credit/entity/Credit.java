package com.ntt.data.ms.credit.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ntt.data.ms.credit.config.ObjectIdSerializer;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
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
}
