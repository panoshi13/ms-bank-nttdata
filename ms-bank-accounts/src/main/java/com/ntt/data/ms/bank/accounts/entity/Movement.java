package com.ntt.data.ms.bank.accounts.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class Movement {

    private String type;
    private double amount;
    private LocalDateTime date;
}