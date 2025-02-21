package com.ntt.data.ms.bank.accounts.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditDTO {
    private Double balanceWithInterestRate;

    private Double creditLimit;

    private Double availableBalance;

    private Double monthlyFee;

    private Boolean status;

    private OffsetDateTime grantDate;

    private List<PaymentDTO> payments;
}
