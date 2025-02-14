package com.ntt.data.ms.client.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ntt.data.ms.client.config.ObjectIdSerializer;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Document(collection = "Customers")
public class Customer {
    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private String type;
    private String name;
    private String identification;
    private String phone;
    private String email;
    private String address;

    /*
    @Field("bank_accounts")
    private List<String> bankAccounts;  // Guarda solo los IDs de las cuentas
    private List<String> credits; // Guarda solo los IDs de los créditos
     */
}
