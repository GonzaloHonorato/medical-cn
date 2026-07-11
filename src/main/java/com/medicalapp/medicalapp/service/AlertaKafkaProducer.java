package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Publicador de alertas clinicas en el topico Kafka "alertas".
 * Centraliza el envio para que el procesador de señales, el endpoint publico
 * y el registro directo compartan el mismo camino hacia Kafka.
 */
@Service
public class AlertaKafkaProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertaKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String alertasTopic;

    public AlertaKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${medicalapp.kafka.topic.alertas}") String alertasTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.alertasTopic = alertasTopic;
    }

    public String topic() {
        return alertasTopic;
    }

    public void enviar(EventoClinicoMessage alerta) {
        try {
            String key = alerta.pacienteId() == null ? null : String.valueOf(alerta.pacienteId());
            String payload = objectMapper.writeValueAsString(alerta);
            kafkaTemplate.send(alertasTopic, key, payload);
            LOGGER.info(
                    "Alerta publicada en topico Kafka '{}'. pacienteId={}, tipo={}, severidad={}",
                    alertasTopic,
                    alerta.pacienteId(),
                    alerta.tipo(),
                    alerta.severidad()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("No se pudo convertir la alerta clinica a JSON.", exception);
        }
    }
}
