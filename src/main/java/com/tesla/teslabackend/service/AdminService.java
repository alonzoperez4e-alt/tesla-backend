package com.tesla.teslabackend.service;

import com.tesla.teslabackend.dto.creacion.*;
import com.tesla.teslabackend.entity.*;
import com.tesla.teslabackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired private CursoRepository cursoRepository;
    @Autowired private SemanaRepository semanaRepository;
    @Autowired private LeccionRepository leccionRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private AlternativaRepository alternativaRepository;

    @Transactional
    public Curso crearCurso(CrearCursoDTO dto) {
        Curso curso = new Curso();
        curso.setNombre(dto.nombre());
        curso.setDescripcion(dto.descripcion());
        curso.setIsHabilitado(dto.isHabilitado());
        return cursoRepository.save(curso);
    }

    @Transactional
    public Semana crearSemana(CrearSemanaDTO dto) {
        Curso curso = cursoRepository.findById(dto.idCurso())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        Semana semana = new Semana();
        semana.setCurso(curso);
        semana.setNroSemana(dto.nroSemana());
        semana.setIsBloqueada(dto.isBloqueada());
        return semanaRepository.save(semana);
    }

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

    @Transactional
    public Pregunta crearPreguntaConAlternativas(CrearPreguntaDTO dto) {
        Leccion leccion = leccionRepository.findById(dto.idLeccion())
                .orElseThrow(() -> new RuntimeException("Lección no encontrada"));

        // 1. Crear y guardar la pregunta
        Pregunta pregunta = new Pregunta();
        pregunta.setLeccion(leccion);
        pregunta.setTextoPregunta(dto.textoPregunta());
        pregunta.setSolucionTexto(dto.solucionTexto());
        pregunta.setSolucionImagenUrl(dto.solucionImagenUrl());

        Pregunta preguntaGuardada = preguntaRepository.save(pregunta);

        // 2. Crear y guardar las alternativas asociadas
        if (dto.alternativas() != null && !dto.alternativas().isEmpty()) {
            List<Alternativa> alternativas = dto.alternativas().stream().map(altDto -> {
                Alternativa alt = new Alternativa();
                alt.setPregunta(preguntaGuardada);
                alt.setTextoAlternativa(altDto.texto());
                alt.setIsCorrecta(altDto.isCorrecta());
                return alt;
            }).collect(Collectors.toList());

            alternativaRepository.saveAll(alternativas);
        }

        return preguntaGuardada;
    }
}