package com.ntt.data.ms.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Charge {
    private String description;

    private double amount;

    private LocalDateTime date;
}
