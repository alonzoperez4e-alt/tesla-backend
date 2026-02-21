package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.progress.dto.RankingItemDTO;
import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository; // Ojo: Asegúrate que este Repo esté en el paquete repository global o en progress/repository
import org.springframework.beans.factory.annotation.Autowired; // Usaré Autowired para mantener consistencia con tus otros servicios
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    @Autowired
    private EstadisticasAlumnoRepository estadisticasRepository;

    @Transactional(readOnly = true) // Importante: Optimiza la consulta al ser solo lectura
    public List<RankingItemDTO> obtenerRanking(Integer idUsuarioLogueado) {

        // Traemos a todos ordenados por EXP (Mayor a menor)
        List<EstadisticasAlumno> listaOrdenada = estadisticasRepository.findAllByOrderByExpTotalDesc();
        List<RankingItemDTO> rankingDTOs = new ArrayList<>();

        int posicion = 1;

        for (EstadisticasAlumno stats : listaOrdenada) {

            // Validamos nulos para evitar NullPointerException si el usuario no tiene nombre completo
            String nombre = stats.getUsuario().getNombre();
            String apellido = stats.getUsuario().getApellido();
            String nombreCompleto = (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");

            // Manejo seguro de la inicial
            String inicial = (nombre != null && !nombre.isEmpty())
                    ? nombre.substring(0, 1).toUpperCase()
                    : "?";

            // Lógica de tendencia
            int rankAnt = stats.getRankingAnterior() != null ? stats.getRankingAnterior() : posicion;
            int tendencia = rankAnt - posicion; // Si estaba en puesto 5 y ahora en 3: 5-3 = +2 (Subió)

            // Identificar si es el usuario que hizo la petición
            boolean esYo = stats.getUsuario().getIdUsuario().equals(idUsuarioLogueado);

            RankingItemDTO dto = new RankingItemDTO(
                    stats.getUsuario().getIdUsuario(),
                    nombreCompleto.trim(),
                    inicial,
                    stats.getExpTotal(),
                    posicion,
                    tendencia,
                    esYo
            );

            rankingDTOs.add(dto);
            posicion++;
        }

        return rankingDTOs;
    }
}