package com.tesla.teslabackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TeslabackendApplication {

	public static void main(String[] args) {
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();

			dotenv.entries().forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue())
			);
			System.out.println("✅ Variables de entorno cargadas correctamente.");
		} catch (Exception e) {
			System.err.println("⚠️ No se encontró archivo .env, usando variables del sistema.");
		}

		// 2. Iniciar Spring Boot
		SpringApplication.run(TeslabackendApplication.class, args);
	}
}