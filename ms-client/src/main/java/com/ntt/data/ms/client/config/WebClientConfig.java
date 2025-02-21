package com.ntt.data.ms.client.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("bankAccountApiClient")
    public WebClient bankAccountApiClient(WebClient.Builder webClientBuilder,
                                          @Value("${api.bank.url}") String bankUrl) {
        return webClientBuilder.baseUrl(bankUrl).build();
    }

    @Bean
    @Qualifier("creditApiClient")
    public WebClient creditApiClient(WebClient.Builder webClientBuilder,
                                     @Value("${api.credit.url}") String creditUrl) {
        return webClientBuilder.baseUrl(creditUrl).build();
    }
}
