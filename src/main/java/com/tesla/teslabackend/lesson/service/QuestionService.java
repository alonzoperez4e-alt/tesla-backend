package com.tesla.teslabackend.lesson.service;

import com.tesla.teslabackend.lesson.entity.Alternativa;
import com.tesla.teslabackend.lesson.entity.Leccion;
import com.tesla.teslabackend.lesson.entity.Pregunta;
import com.tesla.teslabackend.lesson.repository.AlternativaRepository;
import com.tesla.teslabackend.lesson.repository.LeccionRepository;
import com.tesla.teslabackend.lesson.repository.PreguntaRepository;
import com.tesla.teslabackend.lesson.dto.CrearPreguntaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private AlternativaRepository alternativaRepository;
    @Autowired private LeccionRepository leccionRepository;

    @Transactional
    public Pregunta crearPreguntaConAlternativas(CrearPreguntaDTO dto) {
        Leccion leccion = leccionRepository.findById(dto.idLeccion())
                .orElseThrow(() -> new RuntimeException("Lección no encontrada"));

        Pregunta pregunta = new Pregunta();
        pregunta.setLeccion(leccion);
        pregunta.setTextoPregunta(dto.textoPregunta());
        pregunta.setSolucionTexto(dto.solucionTexto());
        pregunta.setSolucionImagenUrl(dto.solucionImagenUrl());

        Pregunta preguntaGuardada = preguntaRepository.save(pregunta);

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