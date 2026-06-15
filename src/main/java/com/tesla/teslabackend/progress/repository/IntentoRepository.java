package com.tesla.teslabackend.progress.repository;

import com.tesla.teslabackend.progress.entity.Intento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface IntentoRepository extends JpaRepository<Intento, Integer> {

    boolean existsByUsuarioIdUsuarioAndLeccionIdLeccion(Integer idUsuario, Integer idLeccion);

    @Query("SELECT i.leccion.idLeccion, i.puntaje FROM Intento i WHERE i.usuario.idUsuario = :idUsuario AND i.isPrimerIntento = true")
    List<Object[]> findPuntajesByUsuario(@Param("idUsuario") Integer idUsuario);
    
@Query("SELECT i.usuario.idUsuario, SUM(i.expGanada) FROM Intento i " +
        "WHERE i.isPrimerIntento = true AND i.expGanada > 0 " +
        "AND i.fecha >= :inicio AND i.fecha < :fin " +
        "GROUP BY i.usuario.idUsuario " +
        "ORDER BY SUM(i.expGanada) DESC")
    List<Object[]> findExpAgregadaPorVentana(@Param("inicio") ZonedDateTime inicio, @Param("fin") ZonedDateTime fin);

    @Modifying
    @Query(value = "INSERT INTO intento (id_usuario, id_leccion, puntaje, is_primer_intento, exp_ganada, fecha) " +
            "VALUES (:idUsuario, :idLeccion, :puntaje, true, :expGanada, :fecha) " +
            "ON CONFLICT (id_usuario, id_leccion) WHERE is_primer_intento " +
            "DO NOTHING", nativeQuery = true)
    int registrarPrimerIntentoIdempotente(@Param("idUsuario") Integer idUsuario,
                                          @Param("idLeccion") Integer idLeccion,
                                          @Param("puntaje") Integer puntaje,
                                          @Param("expGanada") Integer expGanada,
                                          @Param("fecha") ZonedDateTime fecha);
}