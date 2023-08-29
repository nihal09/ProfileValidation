package com.intuit.userbusinessprofile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UserBusinessProfileApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserBusinessProfileApplication.class, args);
	}

}
