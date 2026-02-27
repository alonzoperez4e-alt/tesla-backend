package com.tesla.teslabackend.security.auth.service;

import com.tesla.teslabackend.security.auth.dto.AuthenticationRequest;
import com.tesla.teslabackend.security.auth.dto.AuthenticationResponse;
import com.tesla.teslabackend.security.auth.dto.RegisterRequest;

public interface IAuthenticationService {

    AuthenticationResponse register(RegisterRequest registerRequest);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    AuthenticationResponse refresh(String refreshToken);
}
