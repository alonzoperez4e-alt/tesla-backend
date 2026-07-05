package com.tesla.teslabackend.progress.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Garantiza que exista el índice único parcial que usa
 * IntentoRepository#registrarPrimerIntentoIdempotente como árbitro de su
 * ON CONFLICT. Ni ddl-auto=update ni ddl-auto=validate pueden crear índices
 * parciales, así que se asegura aquí en cada arranque (idempotente vía
 * IF NOT EXISTS, nunca debe impedir que la aplicación termine de levantar).
 */
@Component
public class IntentoIndexBootstrap implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(IntentoIndexBootstrap.class);

    private static final String SQL =
            "CREATE UNIQUE INDEX IF NOT EXISTS ux_intento_primer_intento " +
            "ON intento (id_usuario, id_leccion) WHERE is_primer_intento";

    private final DataSource dataSource;

    public IntentoIndexBootstrap(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(SQL);
            logger.info("Índice único parcial de intento (ux_intento_primer_intento) verificado/creado correctamente.");
        } catch (Exception ex) {
            // Nunca debe impedir que la aplicación termine de arrancar.
            logger.error("No se pudo asegurar el índice único parcial de intento; el submit de lecciones fallará hasta corregirlo.", ex);
        }
    }
}
