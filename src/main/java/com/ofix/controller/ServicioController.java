package com.ofix.controller;

import com.ofix.dto.ServicioRequest;
import com.ofix.dto.ServicioResponse;
import com.ofix.entity.Rol;
import com.ofix.entity.Usuario;
import com.ofix.service.ServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public List<ServicioResponse> listarServicios(
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return servicioService.listarServiciosPorCliente(usuario.getId());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<ServicioResponse>> listarServiciosDisponibles(
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        if (usuario.getRol() != com.ofix.entity.Rol.TRABAJADOR) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                servicioService.listarServiciosDisponibles(usuario.getId())
        );
    }

    @GetMapping("/mis-servicios")
    public ResponseEntity<List<ServicioResponse>> listarMisServicios(
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        if (usuario.getRol() != Rol.TRABAJADOR) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                servicioService.listarServiciosPorTrabajador(
                        usuario.getId()
                )
        );
    }



    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponse> buscarPorId(
            @PathVariable Long id) {

        return servicioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crea un servicio usando los IDs del cliente y la categoría.
    @PostMapping
    public ResponseEntity<ServicioResponse> crearServicio(
            @RequestBody ServicioRequest request,
            Authentication authentication) {
        
        Usuario usuario =(Usuario) authentication.getPrincipal();

        return servicioService.guardarServicio(request, usuario.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponse> actualizarServicio(
            @PathVariable Long id,
            @RequestBody ServicioRequest request, Authentication authentication) {
        
        Usuario usuario = (Usuario) authentication.getPrincipal();        

        return servicioService.actualizarServicio(id, request, usuario.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Permite que un trabajador autenticado acepte un servicio.
    @PutMapping("/{servicioId}/aceptar")
    public ResponseEntity<ServicioResponse> aceptarServicio(
            @PathVariable Long servicioId,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return servicioService.aceptarServicio(
                        servicioId,
                        usuario.getId()
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Finaliza un servicio cambiando su estado a COMPLETADO.
    @PutMapping("/{servicioId}/finalizar")
    public ResponseEntity<ServicioResponse> finalizarServicio(
            @PathVariable Long servicioId,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return servicioService.finalizarServicio(servicioId, usuario.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Permite al cliente cancelar su propio servicio.
    @PutMapping("/{servicioId}/cancelar")
    public ResponseEntity<ServicioResponse> cancelarServicio(
            @PathVariable Long servicioId,
            Authentication authentication) {
             
        Usuario usuario = (Usuario) authentication.getPrincipal();        

        return servicioService.cancelarServicio(
                        servicioId,
                        usuario.getId()
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long id,Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();
        servicioService.eliminarServicio(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
    

    
}