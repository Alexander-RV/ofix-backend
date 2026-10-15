package com.ofix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicios")
@Getter
@Setter
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Información básica de la solicitud.
    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    // Cuándo quiere el cliente que se realice el servicio.
    private LocalDateTime fechaProgramada;

    // Presupuesto aproximado del cliente.
    @Column(precision = 12, scale = 2)
    private BigDecimal presupuesto;

    // Precio final acordado al aceptar una oferta.
    @Column(precision = 12, scale = 2)
    private BigDecimal precioAcordado;

    @Column(nullable = false)
    private String estado = "PENDIENTE";

    // Cliente que solicita el servicio.
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    // Trabajador que acepta y realiza el servicio
    @ManyToOne
    @JoinColumn(name = "trabajador_id")
    private Usuario trabajador;

    // Categoría a la que pertenece el servicio.
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaServicio categoria;
}