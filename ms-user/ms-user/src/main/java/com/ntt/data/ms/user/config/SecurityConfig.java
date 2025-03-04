package com.ntt.data.ms.user.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests -> {
                    requests.requestMatchers(HttpMethod.GET,"/login**","/oauth2/**").permitAll();
                    requests.requestMatchers(HttpMethod.POST,"/login**","/oauth2/**","/user/**").permitAll();
                    requests.anyRequest().authenticated();
                })
            .oauth2Login(Customizer.withDefaults())
            .build();
    }
}


