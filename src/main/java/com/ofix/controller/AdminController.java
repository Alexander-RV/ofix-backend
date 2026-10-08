package com.ofix.controller;

import com.ofix.dto.ServicioResponse;
import com.ofix.dto.UsuarioResponse;
import com.ofix.entity.Usuario;
import com.ofix.exception.ReglaNegocioException;
import com.ofix.service.ServicioService;
import com.ofix.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Endpoints exclusivos del panel administrativo (React).
// La restricción por rol se define en SecurityConfig: /api/admin/** -> ADMIN
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final ServicioService servicioService;

    public AdminController(
            UsuarioService usuarioService,
            ServicioService servicioService) {

        this.usuarioService = usuarioService;
        this.servicioService = servicioService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    // Todas las solicitudes (servicios) de la plataforma, para el dashboard.
    @GetMapping("/servicios")
    public List<ServicioResponse> listarServicios() {
        return servicioService.listarServicios();
    }

    // Suspende o reactiva una cuenta. Body: { "activo": true | false }
    @PutMapping("/usuarios/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            Authentication authentication) {

        Usuario admin = (Usuario) authentication.getPrincipal();

        if (admin.getId().equals(id)) {
            throw new ReglaNegocioException("No puedes suspender tu propia cuenta");
        }

        Boolean activo = body.get("activo");
        if (activo == null) {
            throw new ReglaNegocioException("El campo 'activo' es obligatorio");
        }

        return usuarioService.cambiarEstado(id, activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}