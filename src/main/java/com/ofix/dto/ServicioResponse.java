package com.ofix.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ServicioResponse {

    private Long id;

    private String titulo;

    private String descripcion;

    private String direccion;

    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaProgramada;

    private BigDecimal presupuesto;

    private BigDecimal precioAcordado;

    private String estado;

    // Información básica del cliente.
    private Long clienteId;
    private String clienteNombre;

    // Información del trabajador que realiza el servicio.
    private Long trabajadorId;
    private String trabajadorNombre;

    // Información básica de la categoría.
    private Long categoriaId;
    private String categoriaNombre;

    // Cantidad de ofertas pendientes que tiene la solicitud.
    private Long totalOfertas;

    // Solo en "disponibles": si el profesional ya ofertó.
    private Boolean yaOfertado;

}