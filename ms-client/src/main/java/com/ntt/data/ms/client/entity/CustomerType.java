package com.ntt.data.ms.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerType implements Serializable {

    private static final long serialVersionUID = 1L;

    private ClientType customerType;

    private ProfileType profile;
}
