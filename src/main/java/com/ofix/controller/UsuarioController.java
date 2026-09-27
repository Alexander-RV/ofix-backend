package com.ofix.controller;

import jakarta.validation.Valid;
import com.ofix.dto.UsuarioRequest;
import com.ofix.dto.UsuarioResponse;
import com.ofix.entity.Usuario;
import com.ofix.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.ofix.dto.ActualizarPerfilRequest;
import com.ofix.dto.CambiarPasswordRequest;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioActual(
            Authentication authentication) {

        Usuario usuario =
                (Usuario) authentication.getPrincipal();

        return usuarioService.buscarPorId(usuario.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable Long id) {

        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(
            @Valid @RequestBody UsuarioRequest request) {

        UsuarioResponse nuevoUsuario =
                usuarioService.guardarUsuario(request);

        return ResponseEntity.ok(nuevoUsuario);
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarMiPerfil(
            @Valid @RequestBody ActualizarPerfilRequest request,
            Authentication authentication) {

        Usuario usuario =
                (Usuario) authentication.getPrincipal();

        return usuarioService.actualizarPerfil(
                usuario.getId(),
                request
        )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> cambiarMiPassword(
            @Valid @RequestBody CambiarPasswordRequest request,
            Authentication authentication) {

        Usuario usuario =
                (Usuario) authentication.getPrincipal();

        usuarioService.cambiarPassword(
                usuario.getId(),
                request
        );

        return ResponseEntity.noContent().build();
    }



    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request,
            Authentication authentication) {

        Usuario usuarioAutenticado =
                (Usuario) authentication.getPrincipal();

        return usuarioService.actualizarUsuario(
                id,
                request,
                usuarioAutenticado.getId()
        )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable Long id) {

        usuarioService.eliminarUsuario(id);

        return ResponseEntity.noContent().build();
    }
}