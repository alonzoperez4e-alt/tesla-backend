package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.progress.entity.ProgresoLecciones;
import com.tesla.teslabackend.progress.repository.ProgresoLeccionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    @Autowired
    private ProgresoLeccionesRepository progresoRepository;

    @Transactional(readOnly = true)
    public Map<Integer, Boolean> obtenerMapaLeccionesCompletadas(Integer usuarioId, Integer cursoId) {
        List<ProgresoLecciones> progresos = progresoRepository.findProgresoPorUsuarioYCurso(usuarioId, cursoId);

        // Devolvemos directamente el mapa listo para usar
        return progresos.stream()
                .collect(Collectors.toMap(
                        p -> p.getLeccion().getIdLeccion(),
                        ProgresoLecciones::getCompletada,
                        (existente, reemplazo) -> existente
                ));
    }
}