package com.ofix.controller;

import com.ofix.dto.RechazarProfesionalRequest;
import com.ofix.dto.UsuarioResponse;
import com.ofix.entity.EstadoVerificacion;
import com.ofix.service.UsuarioService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Revisión de profesionales. Protegido por SecurityConfig (/api/admin/** = ADMIN).
@RestController
@RequestMapping("/api/admin/profesionales")
public class AdminProfesionalController {

    private final UsuarioService usuarioService;

    public AdminProfesionalController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // GET /api/admin/profesionales            -> todos
    // GET /api/admin/profesionales?estado=PENDIENTE -> solo los pendientes
    @GetMapping
    public List<UsuarioResponse> listar(
            @RequestParam(required = false) EstadoVerificacion estado) {

        return usuarioService.listarProfesionales(estado);
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<UsuarioResponse> aprobar(@PathVariable Long id) {
        return usuarioService.aprobarProfesional(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<UsuarioResponse> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazarProfesionalRequest request) {

        return usuarioService.rechazarProfesional(id, request.getMotivo())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}