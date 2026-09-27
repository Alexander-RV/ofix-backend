package com.ofix.dto;

import com.ofix.entity.Rol;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioResponse {

    private Long id;

    private String nombre;

    private String apellido;

    private String email;

    private Rol rol;

    private Boolean activo;

    private LocalDateTime fechaRegistro;
}
