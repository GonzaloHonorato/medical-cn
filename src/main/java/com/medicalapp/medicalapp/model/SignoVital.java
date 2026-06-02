package com.medicalapp.medicalapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "MED_SIGNOS_VITALES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignoVital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private Integer frecuenciaCardiaca;

    @Column(nullable = false)
    private Integer presionSistolica;

    @Column(nullable = false)
    private Integer presionDiastolica;

    @Column(nullable = false)
    private Integer saturacionOxigeno;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(nullable = false)
    private Integer frecuenciaRespiratoria;

    @Column(nullable = false)
    private OffsetDateTime fechaRegistro;
}
