package com.ntt.data.ms.credit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCard {
    private double creditLimit;

    private double balanceUsed;

    private double availableBalance;

    private List<Charge> charges;
}
