package com.ofix.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.ofix.entity.Servicio;
import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long>{

    List<Servicio> findByClienteId(Long clienteId);

    List<Servicio> findByEstadoAndTrabajadorIsNull(String estado);

    List<Servicio> findByTrabajadorIdAndEstado(Long trabajadorId, String estado); 
}
