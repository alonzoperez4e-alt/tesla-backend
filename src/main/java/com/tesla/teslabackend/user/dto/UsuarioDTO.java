package com.tesla.teslabackend.user.dto;

import com.tesla.teslabackend.user.entity.Rol;

import java.time.LocalDateTime;

public record UsuarioDTO(
        Integer idUsuario,
        String codigoUsuario,
        String nombre,
        String apellido,
        Rol rol,
        String area,
        String tipoAlumno,
        LocalDateTime fechaRegistro,
        String cognitoSub,
        boolean pendienteCognito
) {
}
