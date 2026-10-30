package com.ofix.service;

import com.ofix.dto.AuthRequest;
import com.ofix.dto.AuthResponse;
import com.ofix.entity.EstadoVerificacion;
import com.ofix.entity.Rol;
import com.ofix.entity.Usuario;
import com.ofix.exception.CredencialesInvalidasException;
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
                .findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(CredencialesInvalidasException::new);

        // Comparamos la contraseña enviada con el hash BCrypt.
        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword())) {

            throw new CredencialesInvalidasException();
        }

        // Una cuenta suspendida no puede iniciar sesión.
        if (!usuario.getActivo()) {
            throw new ReglaNegocioException(
                    "Tu cuenta está suspendida. Contacta al administrador."
            );
        }

        // Un profesional no entra hasta que el admin lo apruebe.
        // (estado null = usuario anterior a esta función = verificado)
        if (usuario.getRol() == Rol.TRABAJADOR
                && usuario.getEstadoVerificacion() != null
                && usuario.getEstadoVerificacion() != EstadoVerificacion.VERIFICADO) {

            if (usuario.getEstadoVerificacion() == EstadoVerificacion.PENDIENTE) {
                throw new ReglaNegocioException(
                        "Tu registro está en revisión. Te notificaremos cuando "
                        + "sea aprobado; normalmente la revisión toma hasta 24 horas."
                );
            }

            String motivo = usuario.getMotivoRechazo();

            throw new ReglaNegocioException(
                    "Tu solicitud de registro no fue aprobada."
                    + (motivo != null && !motivo.isBlank()
                            ? " Motivo: " + motivo + "."
                            : "")
                    + " Si crees que es un error, contacta a soporte."
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