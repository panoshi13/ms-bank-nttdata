package com.ntt.data.ms.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.ntt.data.ms.client")
@EnableCaching
public class MsClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsClientApplication.class, args);
    }

}
