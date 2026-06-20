package com.medicalapp.medicalapp.dto;

import java.time.OffsetDateTime;

public record EventoPublicadoResponse(
        String estado,
        String exchange,
        String routingKey,
        OffsetDateTime fechaPublicacion
) {
}
