package com.tesla.teslabackend.repository;

import com.tesla.teslabackend.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {
    // Solo mostramos cursos que la academia ha habilitado globalmente
    List<Curso> findByIsHabilitadoTrue();
}