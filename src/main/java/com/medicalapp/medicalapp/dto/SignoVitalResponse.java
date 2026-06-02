package com.medicalapp.medicalapp.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SignoVitalResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        String habitacion,
        Integer frecuenciaCardiaca,
        Integer presionSistolica,
        Integer presionDiastolica,
        Integer saturacionOxigeno,
        BigDecimal temperatura,
        Integer frecuenciaRespiratoria,
        OffsetDateTime fechaRegistro
) {
}
