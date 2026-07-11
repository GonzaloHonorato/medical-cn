package com.medicalapp.medicalapp.dto;

import java.time.OffsetDateTime;

public record EventoPublicadoResponse(
        String estado,
        String topico,
        String clave,
        OffsetDateTime fechaPublicacion
) {
}
