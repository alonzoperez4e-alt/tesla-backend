package com.tesla.teslabackend.service;

import com.tesla.teslabackend.entity.EstadisticasAlumno;
import com.tesla.teslabackend.entity.Usuario;
import com.tesla.teslabackend.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class EstadisticaService {

    @Autowired
    private EstadisticasAlumnoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public EstadisticasAlumno obtenerPorId(Integer idUsuario) {

        EstadisticasAlumno stats = repository.findById(idUsuario).orElseGet(() -> {
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no existe: " + idUsuario));

            EstadisticasAlumno nueva = EstadisticasAlumno.builder()
                    .usuario(usuario) // ✅ CLAVE para @MapsId
                    .rachaActual(0)
                    .expTotal(0)
                    .estadoMascota("Huevo")
                    .ultimaFechaMision(LocalDate.now().minusDays(1))
                    .build();

            return repository.save(nueva);
        });

        stats.verificarYReiniciarRacha();
        stats.calcularEstado();
        return repository.save(stats);
    }

    /**
     * ESTE MÉTODO ES EL QUE HACE QUE LA RACHA SUBA.
     * Se debe llamar cuando el alumno completa una misión/clase.
     */
    @Transactional
    public EstadisticasAlumno actualizarProgreso(Integer idUsuario, int puntosExp) {
        EstadisticasAlumno stats = obtenerPorId(idUsuario); // Obtenemos las stats actuales
        LocalDate hoy = LocalDate.now();
        LocalDate ultimaVez = stats.getUltimaFechaMision();

        if (ultimaVez != null) {
            long diasDiferencia = ChronoUnit.DAYS.between(ultimaVez, hoy);

            if (diasDiferencia == 1) {
                // Si completó ayer y completa hoy: ¡Sube racha!
                stats.setRachaActual(stats.getRachaActual() + 1);
            } else if (diasDiferencia > 1) {
                // Si pasó más de un día: Racha vuelve a 1 (la de hoy)
                stats.setRachaActual(1);
            }
            // Si la diferencia es 0, ya hizo una misión hoy, no subimos racha de nuevo.
        } else {
            stats.setRachaActual(1); // Primera vez
        }

        stats.setExpTotal(stats.getExpTotal() + puntosExp);
        stats.setUltimaFechaMision(hoy);
        stats.calcularEstado(); // Verificamos si el dinosaurio evoluciona

        return repository.save(stats);
    }
}