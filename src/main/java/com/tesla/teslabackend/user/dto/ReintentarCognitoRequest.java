package com.tesla.teslabackend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReintentarCognitoRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
