package com.tesla.teslabackend.service;

import com.tesla.teslabackend.dto.resultado.*;
import com.tesla.teslabackend.dto.calificacion.*;
import com.tesla.teslabackend.dto.examen.*;
import com.tesla.teslabackend.entity.*;
import com.tesla.teslabackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluacionService {

    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private LeccionRepository leccionRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private IntentoRepository intentoRepository;
    @Autowired private ProgresoLeccionesRepository progresoRepository;
    @Autowired private EstadisticasAlumnoRepository estadisticasRepository;

    // 1. Generar el cuestionario para el Frontend (SIN RESPUESTAS)
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

    // 2. Calificar el intento (Reglas de negocio)
    @Transactional
    public ResultadoEvaluacionDTO calificarLeccion(Integer idLeccion, SolicitudCalificacionDTO solicitud) {

        // A. Validaciones iniciales
        Usuario usuario = usuarioRepository.findById(solicitud.idUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Leccion leccion = leccionRepository.findById(idLeccion)
                .orElseThrow(() -> new RuntimeException("Lección no encontrada"));

        List<Pregunta> preguntasBD = preguntaRepository.findByLeccionIdConAlternativas(idLeccion);

        // Mapa para acceso rápido a las preguntas por ID
        Map<Integer, Pregunta> mapaPreguntas = preguntasBD.stream()
                .collect(Collectors.toMap(Pregunta::getIdPregunta, p -> p));

        int respuestasCorrectas = 0;
        List<FeedbackPreguntaDTO> feedbackList = new ArrayList<>();

        // B. Lógica de corrección
        for (RespuestaAlumnoDTO respuesta : solicitud.respuestas()) {
            Pregunta pregunta = mapaPreguntas.get(respuesta.idPregunta());
            if (pregunta == null) continue;

            // Buscar la alternativa correcta en BD
            Optional<Alternativa> alternativaCorrectaBD = pregunta.getAlternativas().stream()
                    .filter(Alternativa::getIsCorrecta)
                    .findFirst();

            boolean esCorrecta = false;
            Integer idCorrecta = null;

            if (alternativaCorrectaBD.isPresent()) {
                idCorrecta = alternativaCorrectaBD.get().getIdAlternativa();
                // Comparamos lo que envió el alumno con la correcta
                if (idCorrecta.equals(respuesta.idAlternativaSeleccionada())) {
                    esCorrecta = true;
                    respuestasCorrectas++;
                }
            }

            // Preparamos el feedback (solución)
            feedbackList.add(new FeedbackPreguntaDTO(
                    pregunta.getIdPregunta(),
                    esCorrecta,
                    idCorrecta,
                    pregunta.getSolucionTexto(),
                    pregunta.getSolucionImagenUrl()
            ));
        }

        // C. Cálculo de Puntos y Ranking
        boolean esPrimerIntento = !intentoRepository.existsByUsuarioIdUsuarioAndLeccionIdLeccion(usuario.getIdUsuario(), idLeccion);
        int expGanada = 0;

        // Regla: Solo suma puntos para ranking si es el primer intento
        if (esPrimerIntento) {
            expGanada = respuestasCorrectas * 30;

            if (expGanada > 0) {
                EstadisticasAlumno stats = estadisticasRepository.findById(usuario.getIdUsuario())
                        .orElse(new EstadisticasAlumno());
                // Si no existía registro, set id
                if (stats.getIdUsuario() == null) stats.setIdUsuario(usuario.getIdUsuario()); // Asumiendo setter o constructor

                stats.setExpTotal((stats.getExpTotal() == null ? 0 : stats.getExpTotal()) + expGanada);
                estadisticasRepository.save(stats);
            }
        }

        // D. Registrar el Intento
        Intento intento = new Intento();
        intento.setUsuario(usuario);
        intento.setLeccion(leccion);
        intento.setPuntaje(respuestasCorrectas); // O porcentaje, según prefieras
        intento.setIsPrimerIntento(esPrimerIntento);
        // La fecha se pone sola por el default current_timestamp en BD o JPA PrePersist
        intentoRepository.save(intento);

        // E. Actualizar Progreso (Camino del curso)
        // Consideramos completada si acierta al menos 1? O todas?
        // Asumiremos que si termina el quizz, se marca completada independientemente de la nota
        // (Modelo Duolingo: aprendes del error, pero avanzas la lección)
        ProgresoLecciones progreso = progresoRepository.findById(new ProgresoLeccionesId(usuario.getIdUsuario(), leccion.getIdLeccion()))
                .orElse(new ProgresoLecciones());

        if (progreso.getId() == null) { // Es nuevo
            ProgresoLeccionesId pid = new ProgresoLeccionesId(usuario.getIdUsuario(), leccion.getIdLeccion());
            progreso.setId(pid);
            progreso.setUsuario(usuario);
            progreso.setLeccion(leccion);
        }

        progreso.setCompletada(true);
        // Calculamos porcentaje simple: (correctas / total) * 100
        int totalPreguntas = preguntasBD.size();
        int porcentaje = totalPreguntas > 0 ? (respuestasCorrectas * 100 / totalPreguntas) : 0;
        progreso.setProgresoPorcentaje(Math.max(progreso.getProgresoPorcentaje() != null ? progreso.getProgresoPorcentaje() : 0, porcentaje));

        progresoRepository.save(progreso);

        return new ResultadoEvaluacionDTO(
                respuestasCorrectas, // o puntaje numérico
                expGanada,
                true, // Aprobada siempre que termine (lógica Duolingo)
                feedbackList
        );
    }
}