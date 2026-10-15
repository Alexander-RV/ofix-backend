package com.ofix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ActualizarPerfilRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    // Datos profesionales: solo se usan si el usuario es TRABAJADOR.
    private String profesion;
    private Integer anosExperiencia;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcionProfesional;

    private String zonaTrabajo;
    private List<Long> categoriaIds;


}