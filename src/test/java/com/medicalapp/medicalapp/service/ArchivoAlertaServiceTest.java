package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ArchivoAlertaServiceTest {

    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void listaSoloArchivosJsonOrdenadosPorFechaDescendente() throws Exception {
        Path antiguo = tempDir.resolve("alerta-paciente-1-saturacion.json");
        Path reciente = tempDir.resolve("alerta-paciente-2-fiebre.json");
        Path ignorado = tempDir.resolve("notas.txt");
        Files.writeString(antiguo, "{\"tipo\":\"SATURACION_CRITICA\"}", StandardCharsets.UTF_8);
        Files.writeString(reciente, "{\"tipo\":\"FIEBRE_ALTA\"}", StandardCharsets.UTF_8);
        Files.writeString(ignorado, "no json", StandardCharsets.UTF_8);
        Files.setLastModifiedTime(antiguo, java.nio.file.attribute.FileTime.fromMillis(1000));
        Files.setLastModifiedTime(reciente, java.nio.file.attribute.FileTime.fromMillis(2000));

        ArchivoAlertaService service = new ArchivoAlertaService(objectMapper, tempDir.toString());

        var archivos = service.listarArchivos();

        assertThat(archivos).hasSize(2);
        assertThat(archivos.get(0).nombre()).isEqualTo("alerta-paciente-2-fiebre.json");
        assertThat(archivos.get(0).contenido().get("tipo").asText()).isEqualTo("FIEBRE_ALTA");
        assertThat(archivos.get(1).nombre()).isEqualTo("alerta-paciente-1-saturacion.json");
    }
}
