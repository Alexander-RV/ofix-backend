package com.ofix.controller;

import com.ofix.dto.OfertaRequest;
import com.ofix.dto.OfertaResponse;
import com.ofix.entity.Usuario;
import com.ofix.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    // Un profesional envía una oferta a una solicitud.
    @PostMapping("/api/servicios/{servicioId}/ofertas")
    public ResponseEntity<OfertaResponse> crearOferta(
            @PathVariable Long servicioId,
            @Valid @RequestBody OfertaRequest request,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                ofertaService.crearOferta(servicioId, usuario.getId(), request));
    }

    // El cliente dueño ve las ofertas de su solicitud.
    @GetMapping("/api/servicios/{servicioId}/ofertas")
    public ResponseEntity<List<OfertaResponse>> listarOfertasDeServicio(
            @PathVariable Long servicioId,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                ofertaService.listarOfertasDeServicio(
                        servicioId, usuario.getId()));
    }

    // El profesional ve las ofertas que envió.
    @GetMapping("/api/ofertas/mis-ofertas")
    public ResponseEntity<List<OfertaResponse>> listarMisOfertas(
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                ofertaService.listarMisOfertas(usuario.getId()));
    }

    @PutMapping("/api/ofertas/{ofertaId}/aceptar")
    public ResponseEntity<OfertaResponse> aceptarOferta(
            @PathVariable Long ofertaId,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                ofertaService.aceptarOferta(ofertaId, usuario.getId()));
    }

    @PutMapping("/api/ofertas/{ofertaId}/rechazar")
    public ResponseEntity<OfertaResponse> rechazarOferta(
            @PathVariable Long ofertaId,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                ofertaService.rechazarOferta(ofertaId, usuario.getId()));
    }

    @PutMapping("/api/ofertas/{ofertaId}/retirar")
    public ResponseEntity<OfertaResponse> retirarOferta(
            @PathVariable Long ofertaId,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                ofertaService.retirarOferta(ofertaId, usuario.getId()));
    }
}