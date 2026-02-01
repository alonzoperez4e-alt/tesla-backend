package com.tesla.teslabackend.repository;

import com.tesla.teslabackend.entity.Intento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntentoRepository extends JpaRepository<Intento, Integer> {

    // Para saber si ya ha intentado esta lección antes (para el ranking)
    boolean existsByUsuarioIdUsuarioAndLeccionIdLeccion(Integer idUsuario, Integer idLeccion);
}