package com.ofix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicioRequest {

    private String titulo;

    private String descripcion;

    private String direccion;

    // ID del cliente que solicita el servicio.
    private Long clienteId;

    // ID de la categoría del servicio.
    private Long categoriaId;
}