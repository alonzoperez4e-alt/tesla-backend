package com.tesla.teslabackend.repository;

import com.tesla.teslabackend.entity.ProgresoLecciones;
import com.tesla.teslabackend.entity.ProgresoLeccionesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgresoLeccionesRepository extends JpaRepository<ProgresoLecciones, ProgresoLeccionesId> {

    // Buscamos todo el progreso del alumno en un curso específico
    // Usamos la relación Leccion -> Semana -> Curso para filtrar
    @Query("SELECT p FROM ProgresoLecciones p WHERE p.usuario.idUsuario = :usuarioId AND p.leccion.semana.curso.idCurso = :cursoId")
    List<ProgresoLecciones> findProgresoPorUsuarioYCurso(@Param("usuarioId") Integer usuarioId, @Param("cursoId") Integer cursoId);
}