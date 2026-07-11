package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.SenalVitalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Publicador de señales vitales en el topico Kafka "senales_vitales".
 * Lo usan tanto el simulador como el endpoint HTTP, para que ambas entradas
 * recorran el mismo pipeline (procesador -> alertas).
 */
@Service
public class SenalVitalKafkaProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SenalVitalKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String senalesTopic;

    public SenalVitalKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${medicalapp.kafka.topic.senales-vitales}") String senalesTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.senalesTopic = senalesTopic;
    }

    public String topic() {
        return senalesTopic;
    }

    public void enviar(SenalVitalMessage lectura) {
        try {
            String key = lectura.pacienteId() == null ? null : String.valueOf(lectura.pacienteId());
            String payload = objectMapper.writeValueAsString(lectura);
            kafkaTemplate.send(senalesTopic, key, payload);
            LOGGER.debug("Señal vital publicada en '{}' para paciente {}", senalesTopic, lectura.pacienteId());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("No se pudo convertir la señal vital a JSON.", exception);
        }
    }
}
