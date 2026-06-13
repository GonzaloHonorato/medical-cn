package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class EventoClinicoQueueConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventoClinicoQueueConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventoClinicoService eventoClinicoService;

    public EventoClinicoQueueConsumer(ObjectMapper objectMapper, EventoClinicoService eventoClinicoService) {
        this.objectMapper = objectMapper;
        this.eventoClinicoService = eventoClinicoService;
    }

    @RabbitListener(queues = "${medicalapp.rabbitmq.queue}")
    public void consumir(byte[] body) {
        try {
            String payload = new String(body, StandardCharsets.UTF_8);
            LOGGER.info("Mensaje recibido desde RabbitMQ para eventos clinicos: {}", payload);
            EventoClinicoMessage message = objectMapper.readValue(payload, EventoClinicoMessage.class);
            eventoClinicoService.registrarDesdeCola(message);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Mensaje RabbitMQ invalido para eventos clinicos.", exception);
        }
    }
}
