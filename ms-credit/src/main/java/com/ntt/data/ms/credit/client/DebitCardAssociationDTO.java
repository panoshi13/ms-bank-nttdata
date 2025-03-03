package com.ntt.data.ms.credit.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DebitCardAssociationDTO {

    private String customerId;

    private String debitCard;

    private double amount;
}
