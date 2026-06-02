package com.medicalapp.medicalapp.dto;

import java.util.List;

public record DashboardResponse(
        long pacientesActivos,
        long alertasActivas,
        List<ResumenPacienteResponse> pacientes,
        List<AlertaResponse> alertas
) {
}
