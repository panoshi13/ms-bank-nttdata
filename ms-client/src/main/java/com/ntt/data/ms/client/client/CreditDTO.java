package com.ntt.data.ms.client.client;

import com.ntt.data.ms.client.dto.Charge;
import com.ntt.data.ms.client.dto.Payment;
import com.ntt.data.ms.client.entity.CreditType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditDTO {
    private String id;

    private CreditType type;

    private Boolean status;

    private Double availableBalance;

    private List<Payment> payments;

    private List<Charge> charges;
}
