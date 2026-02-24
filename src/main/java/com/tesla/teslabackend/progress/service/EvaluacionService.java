package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.lesson.entity.Alternativa;
import com.tesla.teslabackend.lesson.entity.Leccion;
import com.tesla.teslabackend.lesson.entity.Pregunta;
import com.tesla.teslabackend.lesson.repository.LeccionRepository;
import com.tesla.teslabackend.lesson.repository.PreguntaRepository;
import com.tesla.teslabackend.progress.dto.RespuestaAlumnoDTO;
import com.tesla.teslabackend.progress.dto.SolicitudCalificacionDTO;
import com.tesla.teslabackend.progress.dto.resultado.FeedbackPreguntaDTO;
import com.tesla.teslabackend.progress.dto.resultado.ResultadoEvaluacionDTO;
import com.tesla.teslabackend.progress.entity.*;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.progress.repository.IntentoRepository;
import com.tesla.teslabackend.progress.repository.ProgresoLeccionesRepository;
import com.tesla.teslabackend.user.entity.Usuario;
import com.tesla.teslabackend.user.repository.UsuarioRepository;
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

    // Repositorios de su propio dominio (progress)
    @Autowired private IntentoRepository intentoRepository;
    @Autowired private ProgresoLeccionesRepository progresoRepository;
    @Autowired private EstadisticasAlumnoRepository estadisticasRepository;

    @Transactional
    public ResultadoEvaluacionDTO calificarLeccion(Integer idLeccion, Integer idUsuarioAutenticado, SolicitudCalificacionDTO solicitud) {

        // A. Validaciones usando el ID seguro del JWT
        Usuario usuario = usuarioRepository.findById(idUsuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Leccion leccion = leccionRepository.findById(idLeccion)
                .orElseThrow(() -> new RuntimeException("Lección no encontrada"));

        List<Pregunta> preguntasBD = preguntaRepository.findByLeccionIdConAlternativas(idLeccion);
        Map<Integer, Pregunta> mapaPreguntas = preguntasBD.stream()
                .collect(Collectors.toMap(Pregunta::getIdPregunta, p -> p));

        int respuestasCorrectas = 0;
        List<FeedbackPreguntaDTO> feedbackList = new ArrayList<>();

        // B. Lógica de corrección
        for (RespuestaAlumnoDTO respuesta : solicitud.respuestas()) {
            Pregunta pregunta = mapaPreguntas.get(respuesta.idPregunta());
            if (pregunta == null) continue;

            Optional<Alternativa> alternativaCorrectaBD = pregunta.getAlternativas().stream()
                    .filter(Alternativa::getIsCorrecta)
                    .findFirst();

            boolean esCorrecta = false;
            Integer idCorrecta = null;

            if (alternativaCorrectaBD.isPresent()) {
                idCorrecta = alternativaCorrectaBD.get().getIdAlternativa();
                if (idCorrecta.equals(respuesta.idAlternativaSeleccionada())) {
                    esCorrecta = true;
                    respuestasCorrectas++;
                }
            }

            feedbackList.add(new FeedbackPreguntaDTO(
                    pregunta.getIdPregunta(), esCorrecta, idCorrecta,
                    pregunta.getSolucionTexto(), pregunta.getSolucionImagenUrl()
            ));
        }

        // C. Cálculo de Puntos y Ranking
        boolean esPrimerIntento = !intentoRepository.existsByUsuarioIdUsuarioAndLeccionIdLeccion(usuario.getIdUsuario(), leccion.getIdLeccion());
        int expGanada = 0;

        if (esPrimerIntento) {
            expGanada = respuestasCorrectas * 30;
            if (expGanada > 0) {
                EstadisticasAlumno stats = estadisticasRepository.findById(usuario.getIdUsuario())
                        .orElseGet(() -> {
                            EstadisticasAlumno nueva = new EstadisticasAlumno();
                            nueva.setUsuario(usuario);
                            nueva.setExpTotal(0);
                            nueva.setExpSemanal(0);
                            return nueva;
                        });

                stats.setExpTotal((stats.getExpTotal() == null ? 0 : stats.getExpTotal()) + expGanada);
                stats.setExpSemanal((stats.getExpSemanal() == null ? 0 : stats.getExpSemanal()) + expGanada);

                estadisticasRepository.save(stats);
            }
        }

        // D. Registrar el Intento
        Intento intento = new Intento();
        intento.setUsuario(usuario);
        intento.setLeccion(leccion);
        intento.setPuntaje(respuestasCorrectas);
        intento.setIsPrimerIntento(esPrimerIntento);
        intentoRepository.save(intento);

        // E. Actualizar Progreso
        ProgresoLecciones progreso = progresoRepository.findById(new ProgresoLeccionesId(usuario.getIdUsuario(), leccion.getIdLeccion()))
                .orElse(null);

        if (progreso == null) {
            progreso = new ProgresoLecciones();
            progreso.setUsuario(usuario);
            progreso.setLeccion(leccion);
        }

        progreso.setCompletada(true);
        int totalPreguntas = preguntasBD.size();
        int porcentaje = totalPreguntas > 0 ? (respuestasCorrectas * 100 / totalPreguntas) : 0;
        progreso.setProgresoPorcentaje(Math.max(progreso.getProgresoPorcentaje() != null ? progreso.getProgresoPorcentaje() : 0, porcentaje));

        progresoRepository.save(progreso);

        return new ResultadoEvaluacionDTO(respuestasCorrectas, expGanada, true, feedbackList);
    }
}