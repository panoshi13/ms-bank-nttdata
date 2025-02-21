package com.ntt.data.ms.bank.accounts.client.dto;

import lombok.Data;

@Data
public class CustomerTypeDTO {
    private ClientType customerType;

    private ProfileType profile;
}
