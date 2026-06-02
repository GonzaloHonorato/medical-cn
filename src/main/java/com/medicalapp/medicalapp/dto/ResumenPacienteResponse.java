package com.medicalapp.medicalapp.dto;

import com.medicalapp.medicalapp.model.EstadoPaciente;

public record ResumenPacienteResponse(
        Long id,
        String nombre,
        String rut,
        Integer edad,
        String habitacion,
        String diagnostico,
        EstadoPaciente estado,
        SignoVitalResponse ultimoSignoVital
) {
}
