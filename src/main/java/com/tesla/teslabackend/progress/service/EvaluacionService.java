package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.lesson.entity.Alternativa;
import com.tesla.teslabackend.lesson.entity.Leccion;
import com.tesla.teslabackend.lesson.entity.Pregunta;
import com.tesla.teslabackend.lesson.repository.LeccionRepository;
import com.tesla.teslabackend.lesson.repository.PreguntaRepository;
import com.tesla.teslabackend.lesson.storage.service.S3StorageService;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluacionService {

    @Autowired private RankingRedisService rankingRedisService;
    @Autowired private S3StorageService s3StorageService;

    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private LeccionRepository leccionRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // Repositorios de su propio dominio (progress)
    @Autowired private IntentoRepository intentoRepository;
    @Autowired private ProgresoLeccionesRepository progresoRepository;
    @Autowired private EstadisticasAlumnoRepository estadisticasRepository;

    @Transactional
    public ResultadoEvaluacionDTO calificarLeccion(Integer idLeccion, SolicitudCalificacionDTO solicitud, Usuario usuario) {

        // A. Validaciones
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
                    pregunta.getSolucionTexto(), s3StorageService.toPublicUrl(pregunta.getSolucionImagenKey())
            ));
        }

        // C. Cálculo de Puntos y Ranking
        int expGanadaPosible = respuestasCorrectas * 30;
        ZonedDateTime momentoIntento = ZonedDateTime.now();

        // Intentamos insertar atómicamente el "primer intento" en la BD.
        // Si devuelve 1 -> Fue exitoso, ganamos la carrera.
        // Si devuelve 0 -> Hubo conflicto, otro hilo ya lo insertó (es un replay).
        int filasInsertadas = intentoRepository.registrarPrimerIntentoIdempotente(
                usuario.getIdUsuario(),
                leccion.getIdLeccion(),
                respuestasCorrectas,
                expGanadaPosible,
                momentoIntento
        );

        boolean fuePrimerIntentoReal = (filasInsertadas > 0);
        final int expGanadaFinal = fuePrimerIntentoReal ? expGanadaPosible : 0;

        // D. Registrar el Intento
        if (!fuePrimerIntentoReal) {
            // Es un replay. Guardamos un intento normal (is_primer_intento = false).
            // Esto no viola el índice parcial, así que no abortará la transacción.
            Intento replay = new Intento();
            replay.setUsuario(usuario);
            replay.setLeccion(leccion);
            replay.setPuntaje(respuestasCorrectas);
            replay.setIsPrimerIntento(false);
            replay.setExpGanada(0);
            replay.setFecha(momentoIntento);
            intentoRepository.save(replay);
        } else if (expGanadaFinal > 0) {
            // Fuimos el primer intento genuino y el insert ya está en BD.
            // Procedemos a sumar la EXP global y guardar estadísticas.
            EstadisticasAlumno stats = estadisticasRepository.findById(usuario.getIdUsuario())
                    .orElseGet(() -> {
                        EstadisticasAlumno nueva = new EstadisticasAlumno();
                        nueva.setUsuario(usuario);
                        nueva.setExpTotal(0); // Aseguramos no-nulo
                        return nueva;
                    });

            stats.ganarExperiencia(expGanadaFinal);
            estadisticasRepository.save(stats);
        }

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

        if (expGanadaFinal > 0) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            rankingRedisService.registrarExpSemanal(usuario.getIdUsuario(), expGanadaFinal, momentoIntento);
                        }
                    }
            );
        }

        return new ResultadoEvaluacionDTO(respuestasCorrectas, expGanadaFinal, true, feedbackList);
    }
}