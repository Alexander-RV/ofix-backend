package com.ofix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categorias_servicio")
@Getter
@Setter
public class CategoriaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de la categoría del servicio.
    @Column(nullable = false, unique = true)
    private String nombre;

    // Indica si la categoría está disponible.
    @Column(nullable = false)
    private Boolean activo = true;
}