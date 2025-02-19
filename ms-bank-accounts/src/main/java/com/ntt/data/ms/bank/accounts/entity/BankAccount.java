package com.ntt.data.ms.bank.accounts.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ntt.data.ms.bank.accounts.config.ObjectIdSerializer;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "BankAccounts")
public class BankAccount {

    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;

    private AccountType type;

    @Field("client_id")
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId clientId;

    private Double balance;
    private Currency currency;

    @Field("opening_date")
    private LocalDateTime openingDate;

    @Field("maintenance")
    private Double maintenance;

    @Field("limit_movements")
    private Integer limitMovements; // Puede ser null

    @Field("day_withdrawal_deposit")
    private Integer dayWithdrawalDeposit;

    @Field("movements")
    private List<Movement> movements;

    @Field("holders")
    private List<Holder> holders;

    @Field("authorized_signatories")
    private List<AuthorizedSignatories> authorizedSignatories;
}