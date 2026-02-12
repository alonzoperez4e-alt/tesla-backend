package com.tesla.teslabackend.service;

import com.tesla.teslabackend.dto.CaminoCursoDTO;
import com.tesla.teslabackend.dto.LeccionDTO;
import com.tesla.teslabackend.dto.SemanaDTO;
import com.tesla.teslabackend.entity.Curso;
import com.tesla.teslabackend.entity.ProgresoLecciones;
import com.tesla.teslabackend.entity.Semana;
import com.tesla.teslabackend.entity.ProgresoLeccionesId;
import com.tesla.teslabackend.repository.CursoRepository;
import com.tesla.teslabackend.repository.ProgresoLeccionesRepository;
import com.tesla.teslabackend.repository.SemanaRepository;
import com.tesla.teslabackend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AprendizajeService {

    @Autowired private CursoRepository cursoRepository;
    @Autowired private SemanaRepository semanaRepository;
    @Autowired private ProgresoLeccionesRepository progresoRepository;
    @Autowired private UsuarioRepository usuarioRepository; // 1. Inyectamos el repo de usuarios

    @Transactional(readOnly = true)
    public List<Curso> obtenerCursosDisponibles() {
        return cursoRepository.findByIsHabilitadoTrue();
    }

    @Transactional(readOnly = true)
    public CaminoCursoDTO obtenerCaminoDelCurso(Integer cursoId, Integer usuarioId) {

        // --- DEBUG: Imprimir en consola el ID recibido para verificar ---
        System.out.println("AprendizajeService: Solicitando camino para Usuario ID: " + usuarioId + " en Curso ID: " + cursoId);

        // 2. VERIFICACIÓN DE SEGURIDAD (Backend)
        // Aunque React lo valide, nos aseguramos que el ID exista en BD
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
        }

        // 3. Validar curso
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        // 4. Traer estructura (Semanas -> Lecciones)
        // Nota: Asegúrate que este método acepte Long o Integer según tu Repo
        List<Semana> semanasEntity = semanaRepository.findByCursoIdConLecciones(cursoId);

        // 5. Traer progreso SOLO DEL USUARIO SOLICITADO
        List<ProgresoLecciones> progresos = progresoRepository.findProgresoPorUsuarioYCurso(usuarioId, cursoId);

        // 6. Mapear: ID Lección -> Completada (true/false)
        Map<Integer, Boolean> mapaCompletadas = progresos.stream()
                // Asumimos que getLeccion().getId() devuelve Long o Integer compatible
                .collect(Collectors.toMap(
                        p -> p.getLeccion().getIdLeccion(), // Si getId devuelve Integer, Java lo maneja
                        ProgresoLecciones::getCompletada,
                        (existente, reemplazo) -> existente
                ));

        // 7. Construir DTO final
        List<SemanaDTO> semanasDTO = new ArrayList<>();

        for (Semana semana : semanasEntity) {
            List<LeccionDTO> leccionesDTO = new ArrayList<>();

            for (var leccion : semana.getLecciones()) {
                boolean isCompletada = mapaCompletadas.getOrDefault(leccion.getIdLeccion(), false);

                leccionesDTO.add(new LeccionDTO(
                        leccion.getIdLeccion(),
                        leccion.getNombre(),
                        leccion.getOrden(),
                        isCompletada,
                        0 // Aquí iría el puntaje si lo tuvieras en el Progreso
                ));
            }

            // El bloqueo depende solo del Admin
            boolean isBloqueada = semana.getIsBloqueada() != null && semana.getIsBloqueada();

            semanasDTO.add(new SemanaDTO(
                    semana.getIdSemana(),
                    semana.getNroSemana(),
                    isBloqueada,
                    leccionesDTO
            ));
        }

        return new CaminoCursoDTO(curso.getIdCurso(), curso.getNombre(), semanasDTO);
    }
}