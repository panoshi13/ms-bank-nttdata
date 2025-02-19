package com.ntt.data.ms.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerType {
    private ClientType customerType;
    private ProfileType profile;
}
