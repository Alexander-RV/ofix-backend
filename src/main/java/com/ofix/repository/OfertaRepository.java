package com.ofix.repository;

import com.ofix.entity.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    List<Oferta> findByServicioId(Long servicioId);

    List<Oferta> findByServicioIdAndEstado(Long servicioId, String estado);

    // Ofertas visibles para el cliente (sin las retiradas).
    List<Oferta> findByServicioIdAndEstadoNotOrderByFechaCreacionDesc(
            Long servicioId, String estado);

    List<Oferta> findByTrabajadorIdOrderByFechaCreacionDesc(Long trabajadorId);

    boolean existsByServicioIdAndTrabajadorIdAndEstado(
            Long servicioId, Long trabajadorId, String estado);

    boolean existsByServicioIdAndTrabajadorIdAndEstadoIn(
            Long servicioId, Long trabajadorId, Collection<String> estados);

    long countByServicioIdAndEstado(Long servicioId, String estado);
}