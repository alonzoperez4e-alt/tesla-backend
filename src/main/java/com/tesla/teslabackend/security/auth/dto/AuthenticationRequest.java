package com.tesla.teslabackend.security.auth.dto;

public record AuthenticationRequest(
        String codigoUsuario,
        String password
) {
}
