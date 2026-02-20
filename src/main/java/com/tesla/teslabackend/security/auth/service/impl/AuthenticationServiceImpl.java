package com.tesla.teslabackend.security.auth.service.impl;

import com.tesla.teslabackend.entity.Usuario;
import com.tesla.teslabackend.repository.UsuarioRepository;
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
//    @Override
//    public AuthenticationResponse register(RegisterRequest registerRequest) {
//
//        if (usuarioDao.findByEmail(registerRequest.getEmail()).isPresent()) throw new IllegalArgumentException("Email already in use");
//
//        Usuario user = Usuario.builder()
//                .firstname(registerRequest.getFirstname())
//                .lastname(registerRequest.getLastname())
//                .email(registerRequest.getEmail())
//                .phone(registerRequest.getPhone())
//                .birthDate(LocalDate.parse(registerRequest.getBirthdate()))
//                .password(passwordEncoder.encode(registerRequest.getPassword()))
//                .role(Role.CIUDADANO)
//                .build();
//
//
//        usuarioDao.save(user);
//
//        String accessToken = jwtService.generateToken(user, new HashMap<>());
//        String refreshToken = jwtService.generateRefreshToken(user, new HashMap<>());
//
//        return AuthenticationResponse.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .role(user.getRole().name())
//                .build();
//    }

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

        String newAccessToken = jwtService.generateToken(user, claims);
        String newRefreshToken = jwtService.generateRefreshToken(user, claims); // rotación

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(user.getRol().name())
                .build();
    }
}
