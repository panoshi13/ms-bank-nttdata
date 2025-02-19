package com.ntt.data.ms.bank.accounts.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Movement {

    private String type;
    private double amount;
    private LocalDateTime date;
}