package com.tesla.teslabackend.repository;

import com.tesla.teslabackend.entity.EstadisticasAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
    public interface EstadisticasAlumnoRepository extends JpaRepository<EstadisticasAlumno, Integer> {

    @Query("SELECT e FROM EstadisticasAlumno e " +
            "JOIN FETCH e.usuario u " +
            "ORDER BY e.expTotal DESC")
    List<EstadisticasAlumno> findAllByOrderByExpTotalDesc();

}