package com.ntt.data.ms.user.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ntt.data.ms.user.config.ObjectIdSerializer;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "Users")
public class User {

    @MongoId
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;

    private String name;

    private String documentNumber;

    private String documentType;

    private String email;

    private String phone;

    private String imei;

    private Double balance;

    @Field("debit_card")
    private DebitCard debitCard;

}
