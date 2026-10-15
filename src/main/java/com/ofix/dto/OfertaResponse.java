package com.ofix.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OfertaResponse {

    private Long id;
    private BigDecimal precio;
    private String mensaje;
    private String estado;
    private LocalDateTime fechaCreacion;

    // Solicitud a la que pertenece.
    private Long servicioId;
    private String servicioTitulo;
    private String servicioEstado;
    private String categoriaNombre;

    // Profesional que ofertó.
    private Long trabajadorId;
    private String trabajadorNombre;
    private String trabajadorProfesion;
    private Integer trabajadorAnosExperiencia;
    private String trabajadorZona;
}