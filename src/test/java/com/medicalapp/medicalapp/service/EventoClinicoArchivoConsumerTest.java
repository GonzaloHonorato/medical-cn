package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.model.SeveridadAlerta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventoClinicoArchivoConsumerTest {

    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void generaArchivoJsonConElMensajeConsumido() throws Exception {
        EventoClinicoArchivoConsumer consumer = new EventoClinicoArchivoConsumer(objectMapper, tempDir.toString());
        EventoClinicoMessage message = new EventoClinicoMessage(
                1L,
                "SATURACION_CRITICA",
                "Monitor UCI",
                "Saturacion bajo 90%.",
                SeveridadAlerta.ALTA,
                "88%",
                OffsetDateTime.parse("2026-06-25T12:00:00-04:00")
        );

        consumer.consumir(objectMapper.writeValueAsBytes(message));

        List<Path> files = Files.list(tempDir).toList();
        assertThat(files).hasSize(1);
        assertThat(files.get(0).getFileName().toString())
                .startsWith("alerta-paciente-1-saturacion_critica-")
                .endsWith(".json");
        JsonNode json = objectMapper.readTree(Files.readString(files.get(0), StandardCharsets.UTF_8));
        assertThat(json.get("pacienteId").asLong()).isEqualTo(1L);
        assertThat(json.get("tipo").asText()).isEqualTo("SATURACION_CRITICA");
        assertThat(json.get("severidad").asText()).isEqualTo("ALTA");
        assertThat(json.get("valor").asText()).isEqualTo("88%");
    }
}
