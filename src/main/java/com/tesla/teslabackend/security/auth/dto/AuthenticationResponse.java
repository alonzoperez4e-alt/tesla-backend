package com.tesla.teslabackend.security.auth.dto;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        String role
) {
}
