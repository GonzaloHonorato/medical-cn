package com.medicalapp.medicalapp.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public record ArchivoAlertaResponse(
        String nombre,
        Long tamanoBytes,
        OffsetDateTime fechaModificacion,
        JsonNode contenido
) {
}
