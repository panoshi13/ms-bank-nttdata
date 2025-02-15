package com.ntt.data.ms.client.dto;

import lombok.Builder;
import lombok.Data;
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
