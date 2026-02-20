package com.tesla.teslabackend.security.auth.service;

import com.tesla.teslabackend.dto.login.RegisterRequest;
import com.tesla.teslabackend.security.auth.dto.AuthenticationRequest;
import com.tesla.teslabackend.security.auth.dto.AuthenticationResponse;

public interface IAuthenticationService {

    // AuthenticationResponse register(RegisterRequest registerRequest);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    AuthenticationResponse refresh(String refreshToken);
}
