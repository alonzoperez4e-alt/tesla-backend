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

        // 1. VALIDACIONES DE NEGOCIO ESTRICTAS
        validarReglasDePregunta(dto);

        Leccion leccion = leccionRepository.findById(dto.idLeccion())
                .orElseThrow(() -> new RuntimeException("Lección no encontrada"));

        Pregunta pregunta = new Pregunta();
        pregunta.setLeccion(leccion);
        pregunta.setTextoPregunta(dto.textoPregunta());
        pregunta.setSolucionTexto(dto.solucionTexto());

        // 2. Asignación directa de las Object Keys de S3
        pregunta.setPreguntaImagenKey(dto.preguntaImagenKey());
        pregunta.setSolucionImagenKey(dto.solucionImagenKey());

        // 3. Guardar la pregunta en PostgreSQL
        Pregunta preguntaGuardada = preguntaRepository.save(pregunta);

        // 4. Procesar y guardar alternativas
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

    /**
     * Método auxiliar para mantener el código limpio y aplicar Single Responsibility.
     * Valida que la pregunta y la solución tengan al menos texto o imagen.
     */
    private void validarReglasDePregunta(CrearPreguntaDTO dto) {
        boolean tieneTextoPregunta = dto.textoPregunta() != null && !dto.textoPregunta().trim().isEmpty();
        boolean tieneImagenPregunta = dto.preguntaImagenKey() != null && !dto.preguntaImagenKey().trim().isEmpty();

        if (!tieneTextoPregunta && !tieneImagenPregunta) {
            throw new IllegalArgumentException("Validación fallida: La pregunta debe contener texto o una imagen.");
        }

        boolean tieneTextoSolucion = dto.solucionTexto() != null && !dto.solucionTexto().trim().isEmpty();
        boolean tieneImagenSolucion = dto.solucionImagenKey() != null && !dto.solucionImagenKey().trim().isEmpty();

        if (!tieneTextoSolucion && !tieneImagenSolucion) {
            throw new IllegalArgumentException("Validación fallida: La solución debe contener texto o una imagen.");
        }

        boolean tieneAlternativaCorrecta = dto.alternativas() != null &&
                dto.alternativas().stream().anyMatch(a -> Boolean.TRUE.equals(a.isCorrecta()));

        if (!tieneAlternativaCorrecta) {
            throw new IllegalArgumentException("Validación fallida: Debe seleccionar al menos una alternativa como correcta.");
        }
    }
}