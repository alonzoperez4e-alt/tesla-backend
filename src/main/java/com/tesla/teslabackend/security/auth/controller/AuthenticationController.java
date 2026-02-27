package com.tesla.teslabackend.security.auth.controller;

import com.tesla.teslabackend.security.auth.dto.AuthenticationRequest;
import com.tesla.teslabackend.security.auth.dto.AuthenticationResponse;
import com.tesla.teslabackend.security.auth.dto.RegisterRequest;
import com.tesla.teslabackend.security.auth.service.IAuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthenticationResponse auth = authenticationService.register(request);

        addRefreshCookie(response, auth.refreshToken());

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(auth.accessToken())
                .role(auth.role())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request,
            HttpServletResponse response
    ) {
        AuthenticationResponse auth = authenticationService.authenticate(request);

        addRefreshCookie(response, auth.refreshToken());

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(auth.accessToken())
                .role(auth.role())
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthenticationResponse auth = authenticationService.refresh(refreshToken);

        addRefreshCookie(response, auth.refreshToken());

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(auth.accessToken())
                .role(auth.role())
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // true en producción (HTTPS)
                .path("/api/v1/auth")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.noContent().build();
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false) // true en producción (HTTPS)
                .path("/api/v1/auth")
                .sameSite("Lax") // usar "None" si frontend/backend están en sitios distintos + HTTPS
                .maxAge(30L * 24 * 60 * 60) // 30 días
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}