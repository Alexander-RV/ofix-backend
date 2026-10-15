package com.ofix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ofertas")
@Getter
@Setter
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Solicitud a la que responde la oferta.
    @ManyToOne(optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    // Profesional que hace la oferta.
    @ManyToOne(optional = false)
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Usuario trabajador;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    // PENDIENTE, ACEPTADA, RECHAZADA o RETIRADA
    @Column(nullable = false)
    private String estado = "PENDIENTE";

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}