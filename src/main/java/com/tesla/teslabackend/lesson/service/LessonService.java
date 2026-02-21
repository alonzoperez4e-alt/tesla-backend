package com.tesla.teslabackend.lesson.service;

import com.tesla.teslabackend.course.entity.Semana;
import com.tesla.teslabackend.lesson.dto.examen.AlternativaDTO;
import com.tesla.teslabackend.lesson.dto.examen.CuestionarioDTO;
import com.tesla.teslabackend.lesson.dto.examen.PreguntaDTO;
import com.tesla.teslabackend.lesson.dto.CrearLeccionDTO;
import com.tesla.teslabackend.lesson.entity.Leccion;
import com.tesla.teslabackend.lesson.entity.Pregunta;
import com.tesla.teslabackend.lesson.repository.LeccionRepository;
import com.tesla.teslabackend.lesson.repository.PreguntaRepository;
import com.tesla.teslabackend.course.repository.SemanaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonService {

    @Autowired private LeccionRepository leccionRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private SemanaRepository semanaRepository;

    @Transactional
    public Leccion crearLeccion(CrearLeccionDTO dto) {
        Semana semana = semanaRepository.findById(dto.idSemana())
                .orElseThrow(() -> new RuntimeException("Semana no encontrada"));

        Leccion leccion = new Leccion();
        leccion.setSemana(semana);
        leccion.setNombre(dto.nombre());
        leccion.setDescripcion(dto.descripcion());
        leccion.setOrden(dto.orden());
        return leccionRepository.save(leccion);
    }

    @Transactional(readOnly = true)
    public CuestionarioDTO obtenerCuestionario(Integer idLeccion) {
        Leccion leccion = leccionRepository.findById(idLeccion)
                .orElseThrow(() -> new RuntimeException("Lección no encontrada"));

        List<Pregunta> preguntasEntity = preguntaRepository.findByLeccionIdConAlternativas(idLeccion);

        List<PreguntaDTO> preguntasDTO = preguntasEntity.stream().map(p -> new PreguntaDTO(
                p.getIdPregunta(),
                p.getTextoPregunta(),
                p.getAlternativas().stream()
                        .map(a -> new AlternativaDTO(a.getIdAlternativa(), a.getTextoAlternativa()))
                        .collect(Collectors.toList())
        )).collect(Collectors.toList());

        return new CuestionarioDTO(leccion.getIdLeccion(), leccion.getNombre(), preguntasDTO);
    }
}