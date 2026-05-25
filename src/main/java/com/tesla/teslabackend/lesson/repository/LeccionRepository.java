package com.tesla.teslabackend.lesson.repository;

import com.tesla.teslabackend.course.entity.Semana;
import com.tesla.teslabackend.lesson.entity.Leccion;
import com.tesla.teslabackend.lesson.entity.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeccionRepository extends JpaRepository<Leccion, Integer>{

    int countBySemana(Semana semana);

    @Query("""
SELECT l FROM Leccion l
JOIN FETCH l.preguntas p
JOIN FETCH p.alternativas
WHERE l.semana.idSemana = :idSemana
""")
    List<Leccion> findBySemanaIdConPreguntas(Integer idSemana);
}
