package com.tesla.teslabackend.controller;

import com.tesla.teslabackend.dto.login.AuthResponse;
import com.tesla.teslabackend.dto.login.LoginRequest;
import com.tesla.teslabackend.dto.login.RegisterRequest;
import com.tesla.teslabackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// @CrossOrigin eliminado: Se gestiona centralizadamente en SecurityConfig para evitar conflictos
public class AuthController {

    private final AuthService authService;

    // Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // NUEVO: Registro Manual para pruebas
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Importación masiva
    @PostMapping(value = "/importar-excel", consumes = {"multipart/form-data"})
    public ResponseEntity<String> importarUsuarios(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(authService.importarUsuariosDesdeExcel(file));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}