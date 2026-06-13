package com.medicalapp.medicalapp.dto;

import com.medicalapp.medicalapp.model.SeveridadAlerta;

import java.time.OffsetDateTime;

public record EventoClinicoResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        String habitacion,
        String tipo,
        String origen,
        String mensaje,
        SeveridadAlerta severidad,
        String valor,
        OffsetDateTime fechaEvento,
        OffsetDateTime fechaRecepcion
) {
}
