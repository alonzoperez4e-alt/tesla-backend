package com.tesla.teslabackend.dto.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String codigo;
    private String nombre;
    private String apellido;
    private String password;
    private String rol;  // Ej: "alumno", "padre"
    private String area; // Ej: "ingenieria", "letras"
    private String tipoAlumno;
}