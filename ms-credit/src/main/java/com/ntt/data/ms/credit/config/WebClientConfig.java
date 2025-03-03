package com.ntt.data.ms.credit.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${api.client.url}")
    private String clientUrl;


    @Value("${api.bank.url}")
    private String bankUrl;

    @Bean
    public WebClient webClientCustomer() {
        return WebClient.builder()
            .baseUrl(clientUrl)
            .build();
    }

    @Bean
    public WebClient webClientBank() {
        return WebClient.builder()
            .baseUrl(bankUrl)
            .build();
    }

}
