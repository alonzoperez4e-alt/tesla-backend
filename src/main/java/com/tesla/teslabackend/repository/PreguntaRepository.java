package com.tesla.teslabackend.repository;

import com.tesla.teslabackend.entity.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {

    // Traemos preguntas y alternativas en una sola consulta (JOIN FETCH)
    // para optimizar el rendimiento y evitar N+1
    @Query("SELECT DISTINCT p FROM Pregunta p LEFT JOIN FETCH p.alternativas WHERE p.leccion.idLeccion = :idLeccion")
    List<Pregunta> findByLeccionIdConAlternativas(@Param("idLeccion") Integer idLeccion);
}