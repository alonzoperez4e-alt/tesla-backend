package com.tesla.teslabackend.user.controller;

import com.tesla.teslabackend.user.dto.CrearUsuarioRequest;
import com.tesla.teslabackend.user.dto.ReintentarCognitoRequest;
import com.tesla.teslabackend.user.dto.UsuarioDTO;
import com.tesla.teslabackend.user.service.UsuarioAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
public class UsuarioAdminController {

    @Autowired
    private UsuarioAdminService usuarioAdminService;

    @PostMapping
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody CrearUsuarioRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioAdminService.crearUsuario(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<List<UsuarioDTO>> listar(@RequestParam(required = false) String estado) {
        boolean soloPendientes = "pendiente".equalsIgnoreCase(estado);
        return ResponseEntity.ok(usuarioAdminService.listarUsuarios(soloPendientes));
    }

    @PostMapping("/{id}/reintentar-cognito")
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<UsuarioDTO> reintentarCognito(
            @PathVariable Integer id,
            @Valid @RequestBody ReintentarCognitoRequest dto) {
        return ResponseEntity.ok(usuarioAdminService.reintentarCognito(id, dto));
    }
}
