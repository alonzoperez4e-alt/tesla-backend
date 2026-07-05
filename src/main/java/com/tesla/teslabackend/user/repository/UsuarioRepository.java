package com.tesla.teslabackend.user.repository;

import com.tesla.teslabackend.user.entity.Rol;
import com.tesla.teslabackend.user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Método para buscar usuario por código para el Login
    Optional<Usuario> findByCodigoUsuario(String codigoUsuario);

    // Método para verificar si ya existe antes de cargar el Excel
    boolean existsByCodigoUsuario(String codigoUsuario);

    Optional<Usuario> findByCognitoSub(String cognitoSub);

    // Usuarios creados en BD pero aún no vinculados a Cognito (pendientes de reintento)
    List<Usuario> findByCognitoSubIsNullOrderByFechaRegistroAsc();

    boolean existsByRol(Rol rol);
}