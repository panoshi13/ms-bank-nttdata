package com.ntt.data.ms.bank.accounts.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holder {
    private String name;

    private String document;

    private String documentType; // "DNI", "RUC", etc.
}
