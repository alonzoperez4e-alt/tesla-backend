package com.tesla.teslabackend.user.dto;

import com.tesla.teslabackend.user.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearUsuarioRequest(
        @NotBlank String codigoUsuario,
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull Rol rol,
        String area,
        String tipoAlumno
) {
}
