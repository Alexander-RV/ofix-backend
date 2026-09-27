package com.ofix.controller;

import com.ofix.dto.AuthRequest;
import com.ofix.dto.AuthResponse;
import com.ofix.dto.PasswordResetRequest;
import com.ofix.dto.ResetPasswordRequest;
import com.ofix.service.AuthService;
import com.ofix.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            AuthService authService,
            PasswordResetService passwordResetService) {

        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        
        passwordResetService.generarToken(
                request.getEmail()
        );

        return ResponseEntity.ok("Solicitud procesada correctamente");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        if (!request.getNuevaPassword()
                .equals(request.getConfirmarPassword())) {

            throw new RuntimeException(
                    "La nueva contraseña y su confirmación no coinciden"
            );
        }

        passwordResetService.restablecerPassword(
                request.getToken(),
                request.getNuevaPassword()
        );

        return ResponseEntity.noContent().build();
    }
}