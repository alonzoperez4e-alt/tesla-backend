package com.tesla.teslabackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TeslabackendApplication {

	public static void main(String[] args) {
		// 1. Cargar las variables del .env al sistema
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing() // No fallar si el archivo no existe (ej. en producción real)
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