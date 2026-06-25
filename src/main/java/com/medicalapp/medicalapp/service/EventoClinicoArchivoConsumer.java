package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EventoClinicoArchivoConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventoClinicoArchivoConsumer.class);
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ObjectMapper objectMapper;
    private final Path alertFilesPath;

    public EventoClinicoArchivoConsumer(
            ObjectMapper objectMapper,
            @Value("${medicalapp.alert-files.path}") String alertFilesPath
    ) {
        this.objectMapper = objectMapper;
        this.alertFilesPath = Path.of(alertFilesPath);
    }

    @RabbitListener(queues = "${medicalapp.rabbitmq.alert-file-queue}")
    public void consumir(byte[] body) {
        try {
            EventoClinicoMessage message = objectMapper.readValue(body, EventoClinicoMessage.class);
            Files.createDirectories(alertFilesPath);
            Path file = alertFilesPath.resolve(nombreArchivo(message));
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
            Files.writeString(file, json, StandardCharsets.UTF_8);
            LOGGER.info("Consumidor 2 genero archivo JSON de alerta medica: {}", file);
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo generar archivo JSON para la alerta medica.", exception);
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
