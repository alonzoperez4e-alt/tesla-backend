package com.tesla.teslabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TeslabackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeslabackendApplication.class, args);
	}
}