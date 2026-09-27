package com.ofix.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ServicioResponse {

    private Long id;

    private String titulo;

    private String descripcion;

    private String direccion;

    private LocalDateTime fechaSolicitud;

    private String estado;

    // Información básica del cliente.
    private Long clienteId;
    private String clienteNombre;

    // Información del trabajador que acepta el servicio.
    private Long trabajadorId;
    private String trabajadorNombre;

    // Información básica de la categoría.
    private Long categoriaId;
    private String categoriaNombre;
}