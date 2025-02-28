package com.ntt.data.ms.bank.accounts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${api.client.url}")
    private String clientUrl;

    @Value("${api.credit.url}")
    private String creditUrl;

    @Bean
    public WebClient customerApiClient() {
        return WebClient.builder()
            .baseUrl(clientUrl)
            .build();
    }

    @Bean
    public WebClient creditApiClient() {
        return WebClient.builder()
            .baseUrl(creditUrl)
            .build();
    }

}
