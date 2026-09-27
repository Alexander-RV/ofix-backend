package com.ofix.controller;

import com.ofix.entity.CategoriaServicio;
import com.ofix.service.CategoriaServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaServicioController {

    private final CategoriaServicioService categoriaService;

    public CategoriaServicioController(
            CategoriaServicioService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaServicio> listarCategorias() {
        return categoriaService.listarCategorias();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaServicio> buscarPorId(
            @PathVariable Long id) {

        return categoriaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crea una nueva categoría.
    @PostMapping
    public ResponseEntity<CategoriaServicio> crearCategoria(
            @RequestBody CategoriaServicio categoriaServicio) {

        return ResponseEntity.ok(
                categoriaService.guardarCategoria(categoriaServicio)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaServicio> actualizarCategoria(
            @PathVariable Long id,
            @RequestBody CategoriaServicio categoriaServicio) {

        return categoriaService.actualizarCategoria(id, categoriaServicio)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(
            @PathVariable Long id) {

        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}