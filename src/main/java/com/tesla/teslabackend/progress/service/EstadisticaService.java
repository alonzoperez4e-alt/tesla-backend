package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.user.entity.Usuario;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.user.repository.UsuarioRepository;
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
                    .usuario(usuario)
                    .rachaActual(0)
                    .expTotal(0)
                    .expSemanal(0) // Inicializado a 0
                    .estadoMascota("Huevo")
                    .ultimaFechaMision(LocalDate.now().minusDays(1))
                    .build();

            return repository.save(nueva);
        });

        stats.verificarYReiniciarRacha();
        stats.calcularEstado();
        return repository.save(stats);
    }

    @Transactional
    public EstadisticasAlumno actualizarProgreso(Integer idUsuario, int puntosExp) {
        EstadisticasAlumno stats = obtenerPorId(idUsuario);
        LocalDate hoy = LocalDate.now();
        LocalDate ultimaVez = stats.getUltimaFechaMision();

        if (ultimaVez != null) {
            long diasDiferencia = ChronoUnit.DAYS.between(ultimaVez, hoy);
            if (diasDiferencia == 1) {
                stats.setRachaActual(stats.getRachaActual() + 1);
            } else if (diasDiferencia > 1) {
                stats.setRachaActual(1);
            }
        } else {
            stats.setRachaActual(1);
        }

        stats.setExpTotal((stats.getExpTotal() != null ? stats.getExpTotal() : 0) + puntosExp);
        stats.setExpSemanal((stats.getExpSemanal() != null ? stats.getExpSemanal() : 0) + puntosExp);

        stats.setUltimaFechaMision(hoy);
        stats.calcularEstado();

        return repository.save(stats);
    }
}