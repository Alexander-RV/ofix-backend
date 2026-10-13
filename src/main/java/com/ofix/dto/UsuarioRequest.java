package com.ofix.dto;

import com.ofix.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
public class UsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    @Pattern (
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
        message = "La contraseña debe de contener mayúscula, minúscula, número y carácter especial"
    )
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    private String profesion;

    private Integer anosExperiencia;

    @Size(max = 500, message = "La descripción debe tener máximo 500 caracteres")
    private String descripcionProfesional;

    private String zonaTrabajo;

    private List<Long> categoriaIds;


}