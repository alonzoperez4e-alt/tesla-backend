package com.tesla.teslabackend.security.auth.service.impl;

import com.tesla.teslabackend.user.entity.Usuario;
import com.tesla.teslabackend.user.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {

        Usuario user = usuarioRepository.findByCodigoUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getCodigoUsuario(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().name()))
        );
    }
}