package com.tesla.teslabackend.progress.repository;

import com.tesla.teslabackend.progress.entity.HistorialRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialRankingRepository extends JpaRepository<HistorialRanking, Integer> {

    @Query("SELECT h FROM HistorialRanking h JOIN FETCH h.usuario " +
            "WHERE h.mes = :mes AND h.anio = :anio " +
            "ORDER BY h.fechaFinSemana DESC, h.posicion ASC")
    List<HistorialRanking> findByMesAndAnio(Integer mes, Integer anio);
}