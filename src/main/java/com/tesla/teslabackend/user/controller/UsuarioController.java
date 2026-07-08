package com.tesla.teslabackend.user.controller;

import com.tesla.teslabackend.user.component.IdentityExtractor;
import com.tesla.teslabackend.user.dto.UsuarioDTO;
import com.tesla.teslabackend.user.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UsuarioController {

    private final IdentityExtractor identityExtractor;

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> me(@AuthenticationPrincipal Jwt jwt) {
        Usuario usuario = identityExtractor.getUsuario(jwt);
        return ResponseEntity.ok(new UsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getCodigoUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getArea(),
                usuario.getTipoAlumno(),
                usuario.getFechaRegistro(),
                usuario.getCognitoSub(),
                false
        ));
    }
}
