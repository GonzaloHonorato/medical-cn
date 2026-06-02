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
@Table(name = "MED_ALERTAS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeveridadAlerta severidad;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private OffsetDateTime fechaRegistro;

    @Column(nullable = false)
    private Boolean atendida = false;
}
