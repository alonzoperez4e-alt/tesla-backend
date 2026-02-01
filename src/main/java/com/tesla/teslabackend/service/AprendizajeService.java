package com.tesla.teslabackend.service;

import com.tesla.teslabackend.dto.CaminoCursoDTO;
import com.tesla.teslabackend.dto.LeccionDTO;
import com.tesla.teslabackend.dto.SemanaDTO;
import com.tesla.teslabackend.entity.Curso;
import com.tesla.teslabackend.entity.ProgresoLecciones;
import com.tesla.teslabackend.entity.Semana;
import com.tesla.teslabackend.repository.CursoRepository;
import com.tesla.teslabackend.repository.ProgresoLeccionesRepository;
import com.tesla.teslabackend.repository.SemanaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AprendizajeService {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private SemanaRepository semanaRepository;

    @Autowired
    private ProgresoLeccionesRepository progresoRepository;

    @Transactional(readOnly = true)
    public List<Curso> obtenerCursosDisponibles() {
        return cursoRepository.findByIsHabilitadoTrue();
    }

    @Transactional(readOnly = true)
    public CaminoCursoDTO obtenerCaminoDelCurso(Integer cursoId, Integer usuarioId) {
        // 1. Validar existencia del curso
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado: " + cursoId));

        // 2. Traer estructura (Semanas + Lecciones) tal cual la definió el Admin
        List<Semana> semanasEntity = semanaRepository.findByCursoIdConLecciones(cursoId);

        // 3. Traer el progreso visual del alumno (qué checks pintar)
        List<ProgresoLecciones> progresos = progresoRepository.findProgresoPorUsuarioYCurso(usuarioId, cursoId);

        // Mapa para búsqueda rápida: ID Lección -> ¿Está completa?
        Map<Integer, Boolean> mapaCompletadas = progresos.stream()
                .collect(Collectors.toMap(
                        p -> p.getLeccion().getIdLeccion(),
                        ProgresoLecciones::getCompletada,
                        (existing, replacement) -> existing // En caso raro de duplicados, mantener el primero
                ));

        // 4. Construir respuesta
        List<SemanaDTO> semanasDTO = new ArrayList<>();

        for (Semana semana : semanasEntity) {
            List<LeccionDTO> leccionesDTO = new ArrayList<>();

            for (var leccion : semana.getLecciones()) {
                // Verificamos si el alumno completó esta lección específica
                boolean isCompletada = mapaCompletadas.getOrDefault(leccion.getIdLeccion(), false);

                leccionesDTO.add(new LeccionDTO(
                        leccion.getIdLeccion(),
                        leccion.getNombre(),
                        leccion.getOrden(),
                        isCompletada,
                        0 // Futuro: puntaje obtenido
                ));
            }
            // El bloqueo depende de la configuración del Admin en BD
            boolean isBloqueada = semana.getIsBloqueada() != null && semana.getIsBloqueada();

            semanasDTO.add(new SemanaDTO(
                    semana.getIdSemana(),
                    semana.getNroSemana(),
                    isBloqueada,
                    leccionesDTO
            ));
        }

        return new CaminoCursoDTO(curso.getIdCurso(), curso.getNombre(), semanasDTO);
    }
}