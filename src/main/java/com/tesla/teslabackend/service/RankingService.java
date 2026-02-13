package com.tesla.teslabackend.service;

import com.tesla.teslabackend.dto.ranking.RankingItemDTO;
import com.tesla.teslabackend.entity.EstadisticasAlumno;
import com.tesla.teslabackend.repository.EstadisticasAlumnoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    private final EstadisticasAlumnoRepository estadisticasRepository;

    public RankingService(EstadisticasAlumnoRepository estadisticasRepository) {
        this.estadisticasRepository = estadisticasRepository;
    }

    public List<RankingItemDTO> obtenerRanking(Integer idUsuarioLogueado) {
        List<EstadisticasAlumno> listaOrdenada = estadisticasRepository.findAllByOrderByExpTotalDesc();
        List<RankingItemDTO> rankingDTOs = new ArrayList<>();

        int posicion = 1;

        for (EstadisticasAlumno stats : listaOrdenada) {

            String nombreCompleto = stats.getUsuario().getNombre() + " " + stats.getUsuario().getApellido();
            String inicial = stats.getUsuario().getNombre().substring(0, 1).toUpperCase();


            int rankAnt = stats.getRankingAnterior() != null ? stats.getRankingAnterior() : posicion;
            int tendencia = rankAnt - posicion;

            boolean esYo = stats.getUsuario().getIdUsuario().equals(idUsuarioLogueado);

            RankingItemDTO dto = new RankingItemDTO(
                    stats.getUsuario().getIdUsuario(),
                    esYo ? "Diego" : nombreCompleto,
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
