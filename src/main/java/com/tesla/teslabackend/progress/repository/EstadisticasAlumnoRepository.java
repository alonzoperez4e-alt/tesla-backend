package com.tesla.teslabackend.progress.repository;

import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadisticasAlumnoRepository extends JpaRepository<EstadisticasAlumno, Integer> {

    @Query("SELECT e FROM EstadisticasAlumno e " +
            "JOIN FETCH e.usuario u " +
            "ORDER BY e.expSemanal DESC")
    List<EstadisticasAlumno> findAllByOrderByExpSemanalDesc();

    // Método para el Job que reinicia el torneo (Pone a 0 solo la exp semanal)
    @Modifying
    @Query("UPDATE EstadisticasAlumno e SET e.expSemanal = 0")
    void reiniciarExperienciaSemanal();
}