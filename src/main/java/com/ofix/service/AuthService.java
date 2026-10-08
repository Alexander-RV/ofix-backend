package com.ofix.service;

import com.ofix.dto.AuthRequest;
import com.ofix.dto.AuthResponse;
import com.ofix.entity.Usuario;
import com.ofix.exception.ReglaNegocioException;
import com.ofix.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(AuthRequest request) {

        // Buscamos el usuario por email.
        Usuario usuario = usuarioRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Credenciales incorrectas"
                        )
                );

        // Comparamos la contraseña enviada con el hash BCrypt.
        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword())) {

            throw new RuntimeException(
                    "Credenciales incorrectas"
            );
        }

        //Una cuenta suspendida no puede iniciar sesión 
        if (!usuario.getActivo()) {
                throw new ReglaNegocioException(
                        "Tu cuenta está suspendida. Contacta al administrador."
                );
        }

        // Generamos el JWT utilizando el email.
        String token = jwtService.generarToken(
                usuario.getEmail()
        );

        return new AuthResponse(
                token,
                "Bearer",
                usuario.getRol().name()
        );
    }
}