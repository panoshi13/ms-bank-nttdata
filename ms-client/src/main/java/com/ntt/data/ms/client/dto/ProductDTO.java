package com.ntt.data.ms.client.dto;

import com.ntt.data.ms.client.client.CreditDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private String name;
    private String identification;
    private List<Movement> movementsBankAccount;
    private CreditDTO movementsCredit;
}
