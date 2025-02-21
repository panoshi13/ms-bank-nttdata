package com.ntt.data.ms.credit.dto;

import com.ntt.data.ms.credit.entity.Charge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpendDTO {
    private String idCard;

    private List<Charge> charges;
}
