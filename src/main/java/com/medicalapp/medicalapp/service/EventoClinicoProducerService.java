package com.medicalapp.medicalapp.service;

import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.dto.EventoPublicadoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * Productor HTTP publico de eventos clinicos.
 * Recibe un evento desde el endpoint publico, lo normaliza y lo publica en el
 * topico Kafka "alertas" (mismo camino que las anomalias del procesador).
 */
@Service
public class EventoClinicoProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventoClinicoProducerService.class);

    private final AlertaKafkaProducer alertaKafkaProducer;

    public EventoClinicoProducerService(AlertaKafkaProducer alertaKafkaProducer) {
        this.alertaKafkaProducer = alertaKafkaProducer;
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

        alertaKafkaProducer.enviar(message);
        LOGGER.info(
                "Evento clinico publicado en Kafka. topic={}, pacienteId={}, tipo={}",
                alertaKafkaProducer.topic(),
                message.pacienteId(),
                message.tipo()
        );
        String clave = message.pacienteId() == null ? null : String.valueOf(message.pacienteId());
        return new EventoPublicadoResponse("PUBLICADO", alertaKafkaProducer.topic(), clave, ahora);
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
