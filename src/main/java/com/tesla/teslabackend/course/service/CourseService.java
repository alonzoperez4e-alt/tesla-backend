package com.tesla.teslabackend.course.service;

import com.tesla.teslabackend.course.dto.*;
import com.tesla.teslabackend.course.entity.Curso;
import com.tesla.teslabackend.course.entity.Semana;

import com.tesla.teslabackend.course.repository.CursoRepository;
import com.tesla.teslabackend.course.repository.SemanaRepository;
import com.tesla.teslabackend.progress.service.ProgressService; // Importamos el nuevo servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CursoRepository cursoRepository;
    @Autowired private SemanaRepository semanaRepository;
    @Autowired private ProgressService progressService; // <--- Comunicación entre features

    @Transactional
    public Curso crearCurso(CrearCursoDTO dto) {
        Curso curso = new Curso();
        curso.setNombre(dto.nombre());
        curso.setDescripcion(dto.descripcion());
        curso.setIsHabilitado(dto.isHabilitado());
        return cursoRepository.save(curso);
    }

    @Transactional(readOnly = true)
    public List<CursoDTO> obtenerCursosDisponibles() {
        return cursoRepository.findByIsHabilitadoTrue().stream().map(c -> new CursoDTO(
                c.getIdCurso(),
                c.getNombre(),
                c.getDescripcion(),
                c.getIsHabilitado()
        )).toList();
    }

    @Transactional(readOnly = true)
    public CaminoCursoDTO obtenerCaminoDelCurso(Integer cursoId, Integer usuarioId) {

        // 1. Validar curso
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        // 2. Traer estructura (Semanas -> Lecciones)
        List<Semana> semanasEntity = semanaRepository.findByCursoIdConLecciones(cursoId);

        // 3. Pedirle al ProgresoService el mapa de lecciones completadas
        Map<Integer, Boolean> mapaCompletadas = progressService.obtenerMapaLeccionesCompletadas(usuarioId, cursoId);

        // 4. Construir DTO final
        List<SemanaDTO> semanasDTO = new ArrayList<>();

        for (Semana semana : semanasEntity) {
            List<LeccionDTO> leccionesDTO = new ArrayList<>();

            for (var leccion : semana.getLecciones()) {
                boolean isCompletada = mapaCompletadas.getOrDefault(leccion.getIdLeccion(), false);

                leccionesDTO.add(new LeccionDTO(
                        leccion.getIdLeccion(),
                        leccion.getNombre(),
                        leccion.getOrden(),
                        isCompletada,
                        0
                ));
            }

            boolean isBloqueada = semana.getIsBloqueada() != null && semana.getIsBloqueada();
            semanasDTO.add(new SemanaDTO(semana.getIdSemana(), semana.getNroSemana(), isBloqueada, leccionesDTO));
        }

        return new CaminoCursoDTO(curso.getIdCurso(), curso.getNombre(), semanasDTO);
    }
}