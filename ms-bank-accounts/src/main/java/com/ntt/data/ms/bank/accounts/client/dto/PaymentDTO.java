package com.ntt.data.ms.bank.accounts.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Double amount;

    private LocalDateTime date;
}
