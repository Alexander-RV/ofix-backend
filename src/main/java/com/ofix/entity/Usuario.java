package com.ofix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // Revisión del administrador (solo importa para TRABAJADOR).
    // Se deja sin "nullable = false" para que Hibernate pueda agregar la
    // columna aunque ya existan usuarios; un valor null se trata como VERIFICADO.
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.VERIFICADO;

    // Motivo escrito por el admin cuando rechaza el registro
    @Column(length = 500)
    private String motivoRechazo;

    // Fecha de la última decisión del admin
    private LocalDateTime fechaRevision;

    //Datos profesionales (solo para TRABAJADOR) 

    private String profesion;

    private Integer anosExperiencia;

    @Column(columnDefinition = "TEXT")
    private String descripcionProfesional;

    private String zonaTrabajo;

    @ManyToMany
    @JoinTable(
            name = "trabajador_categorias",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<CategoriaServicio> categorias = new HashSet<>();


}