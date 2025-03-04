package com.ntt.data.ms.user.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateYankiDTO {

    private String debitCardId;

    private double balance;
}
