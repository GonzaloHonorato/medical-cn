package com.medicalapp.medicalapp.dto;

import java.math.BigDecimal;

public record SignoVitalRequest(
        Long pacienteId,
        Integer frecuenciaCardiaca,
        Integer presionSistolica,
        Integer presionDiastolica,
        Integer saturacionOxigeno,
        BigDecimal temperatura,
        Integer frecuenciaRespiratoria
) {
}
