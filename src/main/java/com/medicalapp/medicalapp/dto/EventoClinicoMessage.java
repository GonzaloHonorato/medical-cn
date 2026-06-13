package com.medicalapp.medicalapp.dto;

import com.medicalapp.medicalapp.model.SeveridadAlerta;

import java.time.OffsetDateTime;

public record EventoClinicoMessage(
        Long pacienteId,
        String tipo,
        String origen,
        String mensaje,
        SeveridadAlerta severidad,
        String valor,
        OffsetDateTime fechaEvento
) {
}
