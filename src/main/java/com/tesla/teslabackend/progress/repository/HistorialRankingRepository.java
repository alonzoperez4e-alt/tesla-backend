package com.tesla.teslabackend.progress.repository;

import com.tesla.teslabackend.progress.entity.HistorialRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HistorialRankingRepository extends JpaRepository<HistorialRanking, Integer> {

    @Query("SELECT h FROM HistorialRanking h JOIN FETCH h.usuario " +
            "WHERE h.mes = :mes AND h.anio = :anio AND h.posicion <= 3 " +
            "ORDER BY h.fechaFinSemana DESC, h.posicion ASC")
    List<HistorialRanking> findByMesAndAnio(@Param("mes") Integer mes, @Param("anio") Integer anio);

    @Query("SELECT DISTINCT h.fechaFinSemana FROM HistorialRanking h ORDER BY h.fechaFinSemana DESC")
    List<LocalDate> obtenerFechasDisponibles();

    @Query("SELECT h FROM HistorialRanking h JOIN FETCH h.usuario WHERE h.fechaFinSemana = :fecha ORDER BY h.posicion ASC")
    List<HistorialRanking> findByFechaFinSemanaOrderByPosicionAsc(@Param("fecha") LocalDate fecha);
}