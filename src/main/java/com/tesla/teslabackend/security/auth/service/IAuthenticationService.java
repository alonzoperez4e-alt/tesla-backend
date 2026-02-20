package com.tesla.teslabackend.security.auth.service;

import com.tesla.teslabackend.dto.login.RegisterRequest;
import com.tesla.teslabackend.security.auth.dto.AuthenticationRequest;
import com.tesla.teslabackend.security.auth.dto.AuthenticationResponse;

public interface IAuthenticationService {

    //public AuthenticationResponse register(RegisterRequest registerRequest);
    public AuthenticationResponse authenticate(AuthenticationRequest request);
}
