package com.medicalapp.medicalapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "MED_EVENTOS_CLINICOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(nullable = false, length = 120)
    private String tipo;

    @Column(nullable = false, length = 120)
    private String origen;

    @Column(nullable = false, length = 400)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeveridadAlerta severidad;

    @Column(length = 80)
    private String valor;

    @Column(nullable = false)
    private OffsetDateTime fechaEvento;

    @Column(nullable = false)
    private OffsetDateTime fechaRecepcion;
}
