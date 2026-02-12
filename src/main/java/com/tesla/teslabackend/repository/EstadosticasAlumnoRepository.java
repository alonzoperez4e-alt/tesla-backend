package com.tesla.teslabackend.repository;

import com.tesla.teslabackend.entity.EstadisticasAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface EstadosticasAlumnoRepository extends JpaRepository<EstadisticasAlumno, Integer> {
}