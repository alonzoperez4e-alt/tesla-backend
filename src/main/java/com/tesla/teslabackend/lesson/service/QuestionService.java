package com.tesla.teslabackend.lesson.service;

import com.tesla.teslabackend.lesson.entity.Alternativa;
import com.tesla.teslabackend.lesson.entity.Leccion;
import com.tesla.teslabackend.lesson.entity.Pregunta;
import com.tesla.teslabackend.lesson.repository.AlternativaRepository;
import com.tesla.teslabackend.lesson.repository.LeccionRepository;
import com.tesla.teslabackend.lesson.repository.PreguntaRepository;
import com.tesla.teslabackend.lesson.dto.CrearPreguntaDTO;
import com.tesla.teslabackend.security.infrastructure.S3Service; // <-- CAMBIADO A S3Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private AlternativaRepository alternativaRepository;
    @Autowired private LeccionRepository leccionRepository;

    @Autowired private S3Service s3Service; // <-- CAMBIADO A S3Service

    @Transactional
    public Pregunta crearPreguntaConAlternativas(CrearPreguntaDTO dto, MultipartFile imagenPregunta, MultipartFile imagenSolucion) {

        // 1. VALIDACIONES DE NEGOCIO ESTRICTAS
        validarReglasDePregunta(dto, imagenPregunta, imagenSolucion);

        Leccion leccion = leccionRepository.findById(dto.idLeccion())
                .orElseThrow(() -> new RuntimeException("Lección no encontrada"));

        Pregunta pregunta = new Pregunta();
        pregunta.setLeccion(leccion);
        pregunta.setTextoPregunta(dto.textoPregunta());
        pregunta.setSolucionTexto(dto.solucionTexto());

        try {
            // 2. Subida de imágenes si existen (Ahora apuntando a S3)
            if (imagenPregunta != null && !imagenPregunta.isEmpty()) {
                String urlPregunta = s3Service.uploadFile(imagenPregunta, "academia_tesla/preguntas");
                pregunta.setPreguntaImagenUrl(urlPregunta);
            }

            if (imagenSolucion != null && !imagenSolucion.isEmpty()) {
                String urlSolucion = s3Service.uploadFile(imagenSolucion, "academia_tesla/soluciones");
                pregunta.setSolucionImagenUrl(urlSolucion);
            } else if (dto.solucionImagenUrl() != null) {
                pregunta.setSolucionImagenUrl(dto.solucionImagenUrl());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al subir las imágenes a AWS S3", e); // <-- Mensaje actualizado
        }

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
    private void validarReglasDePregunta(CrearPreguntaDTO dto, MultipartFile imagenPregunta, MultipartFile imagenSolucion) {
        boolean tieneTextoPregunta = dto.textoPregunta() != null && !dto.textoPregunta().trim().isEmpty();
        boolean tieneImagenPregunta = imagenPregunta != null && !imagenPregunta.isEmpty();

        if (!tieneTextoPregunta && !tieneImagenPregunta) {
            throw new IllegalArgumentException("Validación fallida: La pregunta debe contener obligatoriamente texto o una imagen.");
        }

        boolean tieneTextoSolucion = dto.solucionTexto() != null && !dto.solucionTexto().trim().isEmpty();
        boolean tieneImagenSolucion = (imagenSolucion != null && !imagenSolucion.isEmpty()) ||
                (dto.solucionImagenUrl() != null && !dto.solucionImagenUrl().trim().isEmpty());

        if (!tieneTextoSolucion && !tieneImagenSolucion) {
            throw new IllegalArgumentException("Validación fallida: La solución debe contener obligatoriamente texto o una imagen.");
        }

        boolean tieneAlternativaCorrecta = dto.alternativas() != null &&
                dto.alternativas().stream().anyMatch(a -> Boolean.TRUE.equals(a.isCorrecta()));

        if (!tieneAlternativaCorrecta) {
            throw new IllegalArgumentException("Validación fallida: Debe seleccionar al menos una alternativa como correcta.");
        }
    }
}