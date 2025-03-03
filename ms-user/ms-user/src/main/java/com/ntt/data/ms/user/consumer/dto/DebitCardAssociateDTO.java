package com.ntt.data.ms.user.consumer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DebitCardAssociateDTO {
    private String debitCardId;

    private String documentNumber;

    private Double balance;
}
