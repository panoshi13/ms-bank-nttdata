package com.ntt.data.ms.client.client;

import com.ntt.data.ms.client.dto.Movement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountDTO {
    private List<Movement> movements;
}
