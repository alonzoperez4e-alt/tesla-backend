package com.tesla.teslabackend.security.auth.dto;

public record AuthenticationRequest(
        String codigo,
        String password
) {
}
