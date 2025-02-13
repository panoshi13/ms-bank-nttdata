package com.ntt.data.ms.credit.client;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;

@Data
public class ClientDTO {
    private ObjectId id;
    private ClientType type;
    private String name;
    private String identification;
    private String phone;
    private String email;
    private String address;
    private List<String> bankAccounts;  // Guarda solo los IDs de las cuentas
    private List<String> credits; // Guarda solo los IDs de los créditos
}
