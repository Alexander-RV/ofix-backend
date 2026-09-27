package com.ofix.service;

import com.ofix.entity.PasswordResetToken;
import com.ofix.entity.Usuario;
import com.ofix.repository.PasswordResetTokenRepository;
import com.ofix.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void generarToken(String email) {

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No existe un usuario con ese correo"
                        )
                );

        String token = generarTokenSeguro();

        PasswordResetToken passwordResetToken =
                new PasswordResetToken();

        passwordResetToken.setToken(token);
        passwordResetToken.setUsuario(usuario);
        passwordResetToken.setFechaExpiracion(
                LocalDateTime.now().plusMinutes(15)
        );
        passwordResetToken.setUsado(false);

        tokenRepository.save(passwordResetToken);

        emailService.enviarCorreoRecuperacion(usuario.getEmail(), token);

    
    }

    public void restablecerPassword(
            String token,
            String nuevaPassword) {

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El token no es válido"
                                )
                        );

        if (resetToken.getUsado()) {
            throw new RuntimeException(
                    "El token ya fue utilizado"
            );
        }

        if (resetToken.getFechaExpiracion()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "El token ha expirado"
            );
        }

        Usuario usuario = resetToken.getUsuario();

        usuario.setPassword(
                passwordEncoder.encode(nuevaPassword)
        );

        usuarioRepository.save(usuario);

        resetToken.setUsado(true);

        tokenRepository.save(resetToken);
    }

    private String generarTokenSeguro() {

        SecureRandom secureRandom =
                new SecureRandom();

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}