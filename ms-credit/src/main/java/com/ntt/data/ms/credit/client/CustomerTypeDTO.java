package com.ntt.data.ms.credit.client;

import lombok.Data;

@Data
public class CustomerTypeDTO {
    private ClientType customerType;

    private ProfileType profile;
}
