package com.tesla.teslabackend.course.repository;

import com.tesla.teslabackend.course.entity.Semana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SemanaRepository extends JpaRepository<Semana, Integer> {

    // Trae las semanas y sus lecciones
    // Ordenamos por número de semana y orden de lección para mantener la secuencia
    @Query("SELECT DISTINCT s FROM Semana s LEFT JOIN FETCH s.lecciones l WHERE s.curso.idCurso = :idCurso ORDER BY s.nroSemana ASC, l.orden ASC")
    List<Semana> findByCursoIdConLecciones(@Param("idCurso") Integer idCurso);
}