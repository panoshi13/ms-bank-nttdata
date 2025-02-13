package com.ntt.data.ms.credit.dto;

import com.ntt.data.ms.credit.entity.Charge;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpendDTO {
    private String idCard;
    private List<Charge> charges;
}
