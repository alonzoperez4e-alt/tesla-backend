package com.tesla.teslabackend.course.service;

import com.tesla.teslabackend.course.dto.CrearCursoDTO;
import com.tesla.teslabackend.course.dto.CursoDTO;
import com.tesla.teslabackend.course.entity.Curso;
import com.tesla.teslabackend.course.repository.CursoRepository;
import com.tesla.teslabackend.course.repository.SemanaRepository;
import com.tesla.teslabackend.progress.service.ProgressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario del dominio "course": mockea los repositorios y valida la lógica
 * de mapeo y las ramas de negocio sin arrancar contexto de Spring ni tocar
 * infraestructura (BD/Redis/MQ). Estructurado con el patrón AAA.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CursoRepository cursoRepository;
    @Mock private SemanaRepository semanaRepository;
    @Mock private ProgressService progressService;

    @InjectMocks private CourseService courseService;

    /** Helper para construir un curso habilitado sin repetir el builder en cada test. */
    private static Curso cursoHabilitado(int id, String nombre, String descripcion) {
        return Curso.builder()
                .idCurso(id)
                .nombre(nombre)
                .descripcion(descripcion)
                .isHabilitado(true)
                .build();
    }

    @Test
    void crearCurso_habilitaElCursoYLoPersiste() {
        // Arrange
        CrearCursoDTO dto = new CrearCursoDTO("Álgebra", "Curso base", false);
        when(cursoRepository.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Curso resultado = courseService.crearCurso(dto);

        // Assert
        ArgumentCaptor<Curso> captor = ArgumentCaptor.forClass(Curso.class);
        verify(cursoRepository).save(captor.capture());
        Curso persistido = captor.getValue();
        assertThat(persistido.getNombre()).isEqualTo("Álgebra");
        assertThat(persistido.getDescripcion()).isEqualTo("Curso base");
        // El servicio siempre habilita el curso al crearlo, ignorando el flag del DTO.
        assertThat(persistido.getIsHabilitado()).isTrue();
        assertThat(resultado).isSameAs(persistido);
    }

    @Test
    void obtenerCursosDisponibles_mapeaLosCursosHabilitadosADTO() {
        // Arrange
        when(cursoRepository.findByIsHabilitadoTrue())
                .thenReturn(List.of(cursoHabilitado(1, "Álgebra", "Curso base")));

        // Act
        List<CursoDTO> resultado = courseService.obtenerCursosDisponibles();

        // Assert
        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.idCurso()).isEqualTo(1);
            assertThat(dto.nombre()).isEqualTo("Álgebra");
            assertThat(dto.descripcion()).isEqualTo("Curso base");
            assertThat(dto.isHabilitado()).isTrue();
        });
    }

    @Test
    void obtenerCaminoDelCurso_cursoInexistente_lanzaExcepcion() {
        // Arrange
        when(cursoRepository.findById(99)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> courseService.obtenerCaminoDelCurso(99, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Curso no encontrado");
    }
}
