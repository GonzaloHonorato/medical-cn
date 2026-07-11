package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Microservicio de alerta (Tarea 2) - consumidor de auditoria en archivo.
 * Segundo consumer group sobre el topico "alertas": genera un archivo JSON
 * de auditoria por cada alerta recibida. Reproduce el fan-out que antes hacian
 * las dos colas de RabbitMQ.
 */
@Service
public class AlertasArchivoConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertasArchivoConsumer.class);
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ObjectMapper objectMapper;
    private final Path alertFilesPath;

    public AlertasArchivoConsumer(
            ObjectMapper objectMapper,
            @Value("${medicalapp.alert-files.path}") String alertFilesPath
    ) {
        this.objectMapper = objectMapper;
        this.alertFilesPath = Path.of(alertFilesPath);
    }

    @KafkaListener(
            topics = "${medicalapp.kafka.topic.alertas}",
            groupId = "${medicalapp.kafka.group.alerta-archivos:alerta-archivos}"
    )
    public void consumir(String payload) {
        try {
            EventoClinicoMessage message = objectMapper.readValue(payload, EventoClinicoMessage.class);
            Files.createDirectories(alertFilesPath);
            Path file = alertFilesPath.resolve(nombreArchivo(message));
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
            Files.writeString(file, json, StandardCharsets.UTF_8);
            LOGGER.info("Consumidor de archivos genero JSON de alerta medica: {}", file);
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo generar archivo JSON para la alerta medica.", exception);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Mensaje Kafka invalido para el topico de alertas.", exception);
        }
    }

    private String nombreArchivo(EventoClinicoMessage message) {
        String paciente = message.pacienteId() == null ? "sin-paciente" : "paciente-" + message.pacienteId();
        String tipo = normalizar(message.tipo());
        String fecha = FILE_TIMESTAMP.format((message.fechaEvento() == null ? OffsetDateTime.now() : message.fechaEvento()));
        return "alerta-" + paciente + "-" + tipo + "-" + fecha + ".json";
    }

    private String normalizar(String value) {
        if (value == null || value.isBlank()) {
            return "evento";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
