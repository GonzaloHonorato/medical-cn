package com.medicalapp.medicalapp.dto;

import com.medicalapp.medicalapp.model.SeveridadAlerta;

import java.time.OffsetDateTime;

public record AlertaResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        String habitacion,
        String tipo,
        SeveridadAlerta severidad,
        String mensaje,
        OffsetDateTime fechaRegistro
) {
}
