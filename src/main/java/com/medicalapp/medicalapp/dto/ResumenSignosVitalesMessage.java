package com.medicalapp.medicalapp.dto;

import com.medicalapp.medicalapp.model.EstadoPaciente;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ResumenSignosVitalesMessage(
        Integer pacientesActivos,
        Integer lecturasIncluidas,
        OffsetDateTime fechaResumen,
        List<PacienteResumenSignos> pacientes
) {
    public record PacienteResumenSignos(
            Long pacienteId,
            String pacienteNombre,
            String habitacion,
            EstadoPaciente estado,
            UltimoSignoVital ultimoSignoVital
    ) {
    }

    public record UltimoSignoVital(
            Long id,
            Integer frecuenciaCardiaca,
            Integer presionSistolica,
            Integer presionDiastolica,
            Integer saturacionOxigeno,
            BigDecimal temperatura,
            Integer frecuenciaRespiratoria,
            OffsetDateTime fechaRegistro
    ) {
    }
}
