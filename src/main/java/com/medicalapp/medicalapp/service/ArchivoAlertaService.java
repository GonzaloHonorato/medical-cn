package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.ArchivoAlertaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ArchivoAlertaService {

    private final ObjectMapper objectMapper;
    private final Path alertFilesPath;

    public ArchivoAlertaService(
            ObjectMapper objectMapper,
            @Value("${medicalapp.alert-files.path}") String alertFilesPath
    ) {
        this.objectMapper = objectMapper;
        this.alertFilesPath = Path.of(alertFilesPath);
    }

    public List<ArchivoAlertaResponse> listarArchivos() {
        if (!Files.exists(alertFilesPath)) {
            return List.of();
        }
        try (var paths = Files.list(alertFilesPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::esJson)
                    .map(this::mapArchivo)
                    .sorted(Comparator.comparing(ArchivoAlertaResponse::fechaModificacion).reversed())
                    .toList();
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudieron listar los archivos JSON de alertas.", exception);
        }
    }

    private ArchivoAlertaResponse mapArchivo(Path path) {
        try {
            JsonNode contenido = objectMapper.readTree(path.toFile());
            return new ArchivoAlertaResponse(
                    path.getFileName().toString(),
                    Files.size(path),
                    OffsetDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault()),
                    contenido
            );
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo leer el archivo JSON " + path.getFileName() + ".", exception);
        }
    }

    private boolean esJson(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
    }
}
