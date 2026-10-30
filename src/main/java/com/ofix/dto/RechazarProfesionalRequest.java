package com.ofix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de la petición para rechazar un registro: el motivo es obligatorio
@Getter
@Setter
public class RechazarProfesionalRequest {

    @NotBlank(message = "Escribe el motivo del rechazo")
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String motivo;
}