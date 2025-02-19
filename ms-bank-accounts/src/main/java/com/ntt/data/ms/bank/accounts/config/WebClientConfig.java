package com.ntt.data.ms.bank.accounts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient customerApiClient(WebClient.Builder webClientBuilder, @Value("${api.client.url}") String bankUrl) {
        return webClientBuilder.baseUrl(bankUrl).build();
    }

    @Bean
    public WebClient creditApiClient(WebClient.Builder webClientBuilder, @Value("${api.credit.url}") String creditUrl) {
        return webClientBuilder.baseUrl(creditUrl).build();
    }
}
