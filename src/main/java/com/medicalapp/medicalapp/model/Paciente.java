package com.medicalapp.medicalapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MED_PACIENTES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String rut;

    @Column(nullable = false)
    private Integer edad;

    @Column(nullable = false)
    private String habitacion;

    @Column(nullable = false)
    private String diagnostico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPaciente estado;

    @Column(nullable = false)
    private Boolean activo = true;
}
