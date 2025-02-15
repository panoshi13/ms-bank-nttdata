package com.ntt.data.ms.client.client;

import com.ntt.data.ms.client.dto.Charge;
import com.ntt.data.ms.client.dto.Payment;
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
    private List<Payment> payments;
    private List<Charge> charges;

}
