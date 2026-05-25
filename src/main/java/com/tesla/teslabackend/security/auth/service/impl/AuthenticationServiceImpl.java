package com.tesla.teslabackend.security.auth.service.impl;

import com.tesla.teslabackend.security.auth.dto.RegisterRequest;
import com.tesla.teslabackend.user.entity.Rol;
import com.tesla.teslabackend.user.entity.Usuario;
import com.tesla.teslabackend.user.repository.UsuarioRepository;
import com.tesla.teslabackend.security.auth.dto.AuthenticationRequest;
import com.tesla.teslabackend.security.auth.dto.AuthenticationResponse;
import com.tesla.teslabackend.security.auth.service.IAuthenticationService;
import com.tesla.teslabackend.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final UsuarioRepository usuarioDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Habilitar cuando se quiera implementar el registro de usuarios (para cuando mesones quiera o cuando queramos incrementar la cantidad de usuarios)
    @Override
    public AuthenticationResponse register(RegisterRequest registerRequest) {

        if (usuarioDao.findByCodigoUsuario(registerRequest.getCodigo()).isPresent()) throw new IllegalArgumentException("Student Code already in use");

        Usuario user = Usuario.builder()
                .codigoUsuario(registerRequest.getCodigo())
                .nombre(registerRequest.getNombre())
                .apellido(registerRequest.getApellido())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .rol(registerRequest.getRol().equalsIgnoreCase("alumno") ? Rol.alumno : (registerRequest.getRol().equalsIgnoreCase("administrador") ? Rol.administrador : Rol.padre)) // Asignar rol basado en el string recibido
                .area(registerRequest.getArea())
                .tipoAlumno(registerRequest.getTipoAlumno())
                .build();


        usuarioDao.save(user);

        String accessToken = jwtService.generateToken(user, new HashMap<>());
        String refreshToken = jwtService.generateRefreshToken(user, new HashMap<>());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRol().name())
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.codigo(),
                        request.password()
                )
        );

        Usuario user = usuarioDao.findByCodigoUsuario(request.codigo())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return getAuthenticationResponse(user);
    }

    @Override
    public AuthenticationResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token missing");
        }

        String codigoUsuario = jwtService.extractUsername(refreshToken); // o metodo equivalente en tu JwtService
        Usuario user = usuarioDao.findByCodigoUsuario(codigoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        return getAuthenticationResponse(user);
    }

    private AuthenticationResponse getAuthenticationResponse(Usuario user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUsuario", user.getIdUsuario());
        claims.put("nombre", user.getNombre());
        claims.put("apellido", user.getApellido());
        claims.put("role", user.getRol().name()); //Agregamos rol para recibir peticiones del frontend

        String newAccessToken = jwtService.generateToken(user, claims);
        String newRefreshToken = jwtService.generateRefreshToken(user, claims); // rotación

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(user.getRol().name())
                .build();
    }
}
