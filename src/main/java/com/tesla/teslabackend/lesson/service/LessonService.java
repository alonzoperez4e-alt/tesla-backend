package com.tesla.teslabackend.lesson.service;

import com.tesla.teslabackend.course.entity.Semana;
import com.tesla.teslabackend.lesson.dto.detalle.AlternativaDetalleDTO;
import com.tesla.teslabackend.lesson.dto.detalle.LeccionDetalleDTO;
import com.tesla.teslabackend.lesson.dto.detalle.PreguntaDetalleDTO;
import com.tesla.teslabackend.lesson.dto.detalle.SemanaDetalleDTO;
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
                .orElseThrow(() -> new RuntimeException("Semana no encontrada con ID: " + dto.idSemana()));

        // Autocalcular el orden: Contamos cuántas lecciones tiene actualmente esta semana
        // y le sumamos 1 para la nueva lección.
        int cantidadLeccionesActuales = leccionRepository.countBySemana(semana);
        int nuevoOrden = cantidadLeccionesActuales + 1;

        Leccion leccion = new Leccion();
        leccion.setSemana(semana);
        leccion.setNombre(dto.nombre());
        leccion.setDescripcion(dto.descripcion());

        // Asignamos el orden calculado por el backend, ignorando el que mande el DTO
        leccion.setOrden(nuevoOrden);

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
                p.getPreguntaImagenUrl(),
                p.getAlternativas().stream()
                        .map(a -> new AlternativaDTO(a.getIdAlternativa(), a.getTextoAlternativa()))
                        .collect(Collectors.toList())
        )).collect(Collectors.toList());

        return new CuestionarioDTO(leccion.getIdLeccion(), leccion.getNombre(), preguntasDTO);
    }

    @Transactional(readOnly = true)
    public SemanaDetalleDTO verDetalle(Integer idSemana) {
        Semana semana = semanaRepository.findById(idSemana)
                .orElseThrow(() -> new RuntimeException("Semana no encontrada"));

        List<Leccion> leccionesEntity = leccionRepository.findBySemanaIdConPreguntas(idSemana);

        List<LeccionDetalleDTO> leccionDetalleDTO = leccionesEntity.stream().map(l -> new LeccionDetalleDTO(
                l.getIdLeccion(),
                l.getNombre(),
                l.getOrden(),
                l.getPreguntas().stream().map(p -> new PreguntaDetalleDTO(
                        p.getIdPregunta(),
                        p.getTextoPregunta(),
                        p.getPreguntaImagenUrl(),
                        p.getSolucionTexto(),
                        p.getSolucionImagenUrl(),
                        p.getAlternativas().stream().map( a -> new AlternativaDetalleDTO(
                                a.getIdAlternativa(),
                                a.getTextoAlternativa(),
                                a.getIsCorrecta()
                        )).collect(Collectors.toList())
                )).collect(Collectors.toList())
        )).collect(Collectors.toList());
        return new SemanaDetalleDTO(semana.getIdSemana(), semana.getNroSemana(), semana.getIsBloqueada(), leccionDetalleDTO);
    }

    @Transactional
    public void eliminarLeccion(Integer idLeccion) {
        Leccion leccion = leccionRepository.findById(idLeccion)
                .orElseThrow(() -> new RuntimeException("Leccion no encontrada"));
        leccionRepository.delete(leccion);
    }
}