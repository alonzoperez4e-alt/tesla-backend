package com.tesla.teslabackend.progress.service;

import com.tesla.teslabackend.progress.dto.RankingItemDTO;
import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import com.tesla.teslabackend.progress.repository.EstadisticasAlumnoRepository;
import com.tesla.teslabackend.progress.repository.IntentoRepository;
import com.tesla.teslabackend.user.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * El ranking se agrega directamente desde la tabla de intentos tras retirar el
 * cache en Redis, por lo que estos tests fijan el contrato de esa unica ruta.
 */
@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock private EstadisticasAlumnoRepository estadisticasRepository;
    @Mock private IntentoRepository intentoRepository;

    @InjectMocks private RankingService rankingService;

    private static EstadisticasAlumno stats(int idUsuario, String nombre, String apellido, Integer rankingAnterior) {
        Usuario usuario = Usuario.builder()
                .idUsuario(idUsuario)
                .nombre(nombre)
                .apellido(apellido)
                .build();
        return EstadisticasAlumno.builder()
                .idUsuario(idUsuario)
                .usuario(usuario)
                .rankingAnterior(rankingAnterior)
                .build();
    }

    /** Fila tal como la devuelve findExpAgregadaPorVentana: [Integer idUsuario, Long sumaExp]. */
    private static Object[] fila(int idUsuario, long expTotal) {
        return new Object[]{idUsuario, expTotal};
    }

    @Test
    void obtenerRanking_sinIntentosEnLaSemana_devuelveVacioSinConsultarEstadisticas() {
        // Arrange
        when(intentoRepository.findExpAgregadaPorVentana(any(), any())).thenReturn(List.of());

        // Act
        List<RankingItemDTO> resultado = rankingService.obtenerRanking(1);

        // Assert
        assertThat(resultado).isEmpty();
        verifyNoInteractions(estadisticasRepository);
    }

    @Test
    void obtenerRanking_asignaPosicionesCorrelativasYMarcaAlUsuarioLogueado() {
        // Arrange: la consulta ya viene ordenada por exp descendente.
        when(intentoRepository.findExpAgregadaPorVentana(any(), any()))
                .thenReturn(List.of(fila(7, 300L), fila(3, 200L), fila(9, 100L)));
        when(estadisticasRepository.findAllById(any())).thenReturn(List.of(
                stats(7, "Ana", "Lopez", null),
                stats(3, "Beto", "Diaz", null),
                stats(9, "Cira", "Mora", null)
        ));

        // Act
        List<RankingItemDTO> resultado = rankingService.obtenerRanking(3);

        // Assert
        assertThat(resultado).hasSize(3);
        assertThat(resultado).extracting(RankingItemDTO::getPosicionActual).containsExactly(1, 2, 3);
        assertThat(resultado).extracting(RankingItemDTO::getIdUsuario).containsExactly(7, 3, 9);
        assertThat(resultado).extracting(RankingItemDTO::getExpTotal).containsExactly(300, 200, 100);
        assertThat(resultado).extracting(RankingItemDTO::isEsUsuarioActual).containsExactly(false, true, false);

        RankingItemDTO primero = resultado.get(0);
        assertThat(primero.getNombreCompleto()).isEqualTo("Ana Lopez");
        assertThat(primero.getInicial()).isEqualTo("A");
    }

    @Test
    void obtenerRanking_calculaTendenciaDesdeElRankingAnterior() {
        // Arrange: el 7 sube de la posicion 5 a la 1; el 3 no tiene historico.
        when(intentoRepository.findExpAgregadaPorVentana(any(), any()))
                .thenReturn(List.of(fila(7, 300L), fila(3, 200L), fila(9, 100L)));
        when(estadisticasRepository.findAllById(any())).thenReturn(List.of(
                stats(7, "Ana", "Lopez", 5),
                stats(3, "Beto", "Diaz", null),
                stats(9, "Cira", "Mora", 0)
        ));

        // Act
        List<RankingItemDTO> resultado = rankingService.obtenerRanking(1);

        // Assert: sin historico (null o 0) la tendencia es neutra.
        assertThat(resultado).extracting(RankingItemDTO::getTendencia).containsExactly(4, 0, 0);
    }

    @Test
    void obtenerRanking_omiteALosQuePuntuaronPeroNoTienenEstadisticas() {
        // Arrange: el usuario 3 puntuo pero no tiene fila en estadisticas_alumno.
        when(intentoRepository.findExpAgregadaPorVentana(any(), any()))
                .thenReturn(List.of(fila(7, 300L), fila(3, 200L), fila(9, 100L)));
        when(estadisticasRepository.findAllById(any())).thenReturn(List.of(
                stats(7, "Ana", "Lopez", null),
                stats(9, "Cira", "Mora", null)
        ));

        // Act
        List<RankingItemDTO> resultado = rankingService.obtenerRanking(1);

        // Assert: el omitido no consume posicion, el siguiente ocupa la 2.
        assertThat(resultado).extracting(RankingItemDTO::getIdUsuario).containsExactly(7, 9);
        assertThat(resultado).extracting(RankingItemDTO::getPosicionActual).containsExactly(1, 2);
    }

    @Test
    void obtenerRanking_recortaElTopA100Participantes() {
        // Arrange: 150 participantes con exp descendente.
        List<Object[]> agregado = new ArrayList<>();
        List<EstadisticasAlumno> estadisticas = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            agregado.add(fila(i, 1000L - i));
            estadisticas.add(stats(i, "Alumno" + i, "Apellido", null));
        }
        when(intentoRepository.findExpAgregadaPorVentana(any(), any())).thenReturn(agregado);
        when(estadisticasRepository.findAllById(any())).thenReturn(estadisticas);

        // Act
        List<RankingItemDTO> resultado = rankingService.obtenerRanking(1);

        // Assert
        assertThat(resultado).hasSize(100);
        assertThat(resultado.get(0).getIdUsuario()).isZero();
        assertThat(resultado.get(99).getIdUsuario()).isEqualTo(99);
        assertThat(resultado.get(99).getPosicionActual()).isEqualTo(100);
    }
}
