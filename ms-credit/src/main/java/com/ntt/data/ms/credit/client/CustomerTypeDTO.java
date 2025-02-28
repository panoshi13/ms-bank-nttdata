package com.ntt.data.ms.credit.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerTypeDTO {
    private ClientType customerType;

    private ProfileType profile;
}
