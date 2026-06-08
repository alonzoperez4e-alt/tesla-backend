package com.tesla.teslabackend.user.component;

import com.tesla.teslabackend.user.entity.Usuario;
import com.tesla.teslabackend.user.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class IdentityExtractor {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Integer getUsuarioId(Jwt jwt) {
        String cognitoSub = jwt.getSubject();
        return usuarioRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new RuntimeException("Usuario de Cognito no registrado en base de datos"))
                .getIdUsuario();
    }

    public Usuario getUsuario(Jwt jwt) {
        String cognitoSub = jwt.getSubject();
        return usuarioRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new RuntimeException("Usuario de Cognito no registrado en base de datos"));
    }
}
