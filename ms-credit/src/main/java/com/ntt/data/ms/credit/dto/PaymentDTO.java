package com.ntt.data.ms.credit.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private String idCard;
    private double amount;
    private LocalDateTime date;
}
