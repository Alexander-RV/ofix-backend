package com.ofix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ServicioRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 120, message = "El título no puede superar los 120 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    // ID de la categoría del servicio.
    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    // Cuándo quiere el cliente el servicio (opcional).
    private LocalDateTime fechaProgramada;

    // Presupuesto aproximado del cliente (opcional).
    private BigDecimal presupuesto;
}