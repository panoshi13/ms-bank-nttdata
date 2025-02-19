package com.ntt.data.ms.bank.accounts.client.dto;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class ClientDTO {
    private ObjectId id;
    private CustomerTypeDTO type;
    private String name;
    private String identification;
    private String phone;
    private String email;
    private String address;
}
