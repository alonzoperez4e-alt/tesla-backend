package com.tesla.teslabackend.progress.repository;

import com.tesla.teslabackend.progress.entity.Intento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntentoRepository extends JpaRepository<Intento, Integer> {

    boolean existsByUsuarioIdUsuarioAndLeccionIdLeccion(Integer idUsuario, Integer idLeccion);

    @Query("SELECT i.leccion.idLeccion, i.puntaje FROM Intento i WHERE i.usuario.idUsuario = :idUsuario AND i.isPrimerIntento = true")
    List<Object[]> findPuntajesByUsuario(@Param("idUsuario") Integer idUsuario);
}