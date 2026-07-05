package com.tesla.teslabackend.user.bootstrap;

import com.tesla.teslabackend.user.dto.CrearUsuarioRequest;
import com.tesla.teslabackend.user.entity.Rol;
import com.tesla.teslabackend.user.repository.UsuarioRepository;
import com.tesla.teslabackend.user.service.UsuarioAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Siembra la primera cuenta de administrador si aún no existe ninguna. Es
 * idempotente (se puede ejecutar en cada arranque/redeploy) y nunca debe impedir
 * que la aplicación termine de levantar, aunque Cognito o la BD estén caídos.
 */
@Component
public class AdminSeedRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminSeedRunner.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioAdminService usuarioAdminService;

    @Value("${ADMIN_SEED_CODIGO:00000001}")
    private String codigo;

    @Value("${ADMIN_SEED_EMAIL:admin@tesla.edu}")
    private String email;

    @Value("${ADMIN_SEED_NOMBRE:Admin}")
    private String nombre;

    @Value("${ADMIN_SEED_APELLIDO:Principal}")
    private String apellido;

    @Value("${ADMIN_SEED_PASSWORD:TeslaAdmin2026$}")
    private String password;

    @Override
    public void run(String... args) {
        try {
            if (usuarioRepository.existsByRol(Rol.administrador)) {
                logger.info("Ya existe al menos un administrador, se omite el seed inicial.");
                return;
            }

            if (codigo == null || email == null || nombre == null || apellido == null || password == null) {
                logger.info("Variables ADMIN_SEED_* no configuradas por completo, se omite el seed inicial.");
                return;
            }

            usuarioAdminService.crearUsuario(new CrearUsuarioRequest(
                    codigo, nombre, apellido, email, password, Rol.administrador, null, null));

            logger.info("Admin inicial sembrado correctamente: {}", codigo);
        } catch (Exception ex) {
            // Nunca debe impedir que la aplicación termine de arrancar.
            logger.error("Fallo al sembrar el admin inicial (la aplicación continuará arrancando igualmente)", ex);
        }
    }
}
