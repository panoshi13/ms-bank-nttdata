package com.ntt.data.ms.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MsUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsUserApplication.class, args);
	}

}
