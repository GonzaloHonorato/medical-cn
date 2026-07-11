package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Microservicio de alerta (Tarea 2) - consumidor Oracle + WebSocket.
 * Lee el topico "alertas" y guarda el registro de la anomalia en Oracle Cloud
 * (evento clinico + alerta medica) y lo notifica al dashboard Angular por WebSocket.
 */
@Component
public class AlertasOracleConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertasOracleConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventoClinicoService eventoClinicoService;

    public AlertasOracleConsumer(ObjectMapper objectMapper, EventoClinicoService eventoClinicoService) {
        this.objectMapper = objectMapper;
        this.eventoClinicoService = eventoClinicoService;
    }

    @KafkaListener(
            topics = "${medicalapp.kafka.topic.alertas}",
            groupId = "${medicalapp.kafka.group.alerta-oracle:alerta-oracle}"
    )
    public void consumir(String payload) {
        try {
            LOGGER.info("Consumidor Oracle recibio alerta desde Kafka: {}", payload);
            EventoClinicoMessage message = objectMapper.readValue(payload, EventoClinicoMessage.class);
            eventoClinicoService.registrarDesdeCola(message);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Mensaje Kafka invalido para el topico de alertas.", exception);
        }
    }
}
