package com.ntt.data.ms.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Payment {
    private Double amount;

    private LocalDateTime date;
}
