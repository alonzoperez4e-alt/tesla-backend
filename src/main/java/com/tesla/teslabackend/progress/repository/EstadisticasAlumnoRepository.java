package com.tesla.teslabackend.progress.repository;

import com.tesla.teslabackend.progress.entity.EstadisticasAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadisticasAlumnoRepository extends JpaRepository<EstadisticasAlumno, Integer> {
}