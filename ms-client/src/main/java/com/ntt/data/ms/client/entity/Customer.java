package com.ntt.data.ms.client.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ntt.data.ms.client.config.ObjectIdSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "Customers")
@AllArgsConstructor
@NoArgsConstructor
public class Customer implements Serializable {

    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;

    private CustomerType type;

    private String name;

    private String identification;

    private String phone;

    private String email;

    private String address;
}

