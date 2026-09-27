package com.ofix.repository;

import com.ofix.entity.CategoriaServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaServicioRepository
        extends JpaRepository<CategoriaServicio, Long> {

    // Permite buscar una categoría por su nombre.
    Optional<CategoriaServicio> findByNombre(String nombre);
}