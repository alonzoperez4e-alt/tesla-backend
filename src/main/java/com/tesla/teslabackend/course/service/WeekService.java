package com.tesla.teslabackend.course.service;

import com.tesla.teslabackend.course.entity.Semana;
import com.tesla.teslabackend.course.entity.Curso;
import com.tesla.teslabackend.course.repository.CursoRepository;
import com.tesla.teslabackend.course.repository.SemanaRepository;
import com.tesla.teslabackend.course.dto.CrearSemanaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeekService {

    @Autowired private SemanaRepository semanaRepository;
    @Autowired private CursoRepository cursoRepository;

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
}