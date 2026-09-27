package com.ofix.service;

import com.ofix.entity.CategoriaServicio;
import com.ofix.repository.CategoriaServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServicioService {

    private final CategoriaServicioRepository categoriaRepository;

    public CategoriaServicioService(
            CategoriaServicioRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaServicio> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public Optional<CategoriaServicio> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    public CategoriaServicio guardarCategoria(
            CategoriaServicio categoriaServicio) {

        return categoriaRepository.save(categoriaServicio);
    }

    public Optional<CategoriaServicio> actualizarCategoria(
            Long id,
            CategoriaServicio categoriaActualizada) {

        return categoriaRepository.findById(id).map(categoria -> {

            categoria.setNombre(categoriaActualizada.getNombre());
            categoria.setActivo(categoriaActualizada.getActivo());

            return categoriaRepository.save(categoria);
        });
    }

    public void eliminarCategoria(Long id) {
        categoriaRepository.deleteById(id);
    }
}