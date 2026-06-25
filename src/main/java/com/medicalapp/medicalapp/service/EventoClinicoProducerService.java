package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.dto.EventoPublicadoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class EventoClinicoProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventoClinicoProducerService.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String routingKey;

    public EventoClinicoProducerService(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${medicalapp.rabbitmq.exchange}") String exchange,
            @Value("${medicalapp.rabbitmq.alert-routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public EventoPublicadoResponse publicar(EventoClinicoMessage request) {
        validar(request);
        OffsetDateTime ahora = OffsetDateTime.now();
        EventoClinicoMessage message = new EventoClinicoMessage(
                request.pacienteId(),
                request.tipo().trim(),
                textoOValor(request.origen(), "API publica"),
                request.mensaje().trim(),
                request.severidad(),
                textoOpcional(request.valor()),
                request.fechaEvento() == null ? ahora : request.fechaEvento()
        );

        try {
            String payload = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(exchange, routingKey, payload, rabbitMessage -> {
                rabbitMessage.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                rabbitMessage.getMessageProperties().setContentEncoding("UTF-8");
                return rabbitMessage;
            });
            LOGGER.info(
                    "Evento clinico publicado en RabbitMQ. exchange={}, routingKey={}, pacienteId={}, tipo={}",
                    exchange,
                    routingKey,
                    message.pacienteId(),
                    message.tipo()
            );
            return new EventoPublicadoResponse("PUBLICADO", exchange, routingKey, ahora);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("No se pudo convertir el evento clinico a JSON.", exception);
        }
    }

    private void validar(EventoClinicoMessage request) {
        if (request == null) {
            throw new IllegalArgumentException("Debe enviar un evento clinico.");
        }
        if (request.tipo() == null || request.tipo().isBlank()) {
            throw new IllegalArgumentException("El tipo del evento es obligatorio.");
        }
        if (request.mensaje() == null || request.mensaje().isBlank()) {
            throw new IllegalArgumentException("El mensaje del evento es obligatorio.");
        }
    }

    private String textoOValor(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String textoOpcional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
