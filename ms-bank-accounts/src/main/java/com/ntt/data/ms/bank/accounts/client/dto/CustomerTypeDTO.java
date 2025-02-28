package com.ntt.data.ms.bank.accounts.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerTypeDTO {
    private ClientType customerType;

    private ProfileType profile;
}
