package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SeveridadAlerta;
import com.medicalapp.medicalapp.model.SignoVital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SignosVitalesAlertProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignosVitalesAlertProducerService.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String alertRoutingKey;

    public SignosVitalesAlertProducerService(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${medicalapp.rabbitmq.exchange}") String exchange,
            @Value("${medicalapp.rabbitmq.alert-routing-key}") String alertRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.alertRoutingKey = alertRoutingKey;
    }

    public void publicarAlertas(Paciente paciente, SignoVital lectura) {
        List<EventoClinicoMessage> alertas = detectarAlertas(paciente, lectura);
        for (EventoClinicoMessage alerta : alertas) {
            publicar(alerta);
        }
        if (!alertas.isEmpty()) {
            LOGGER.info(
                    "Productor 1 publico {} alerta(s) clinica(s) en RabbitMQ para paciente {}",
                    alertas.size(),
                    paciente.getId()
            );
        }
    }

    private List<EventoClinicoMessage> detectarAlertas(Paciente paciente, SignoVital lectura) {
        List<EventoClinicoMessage> alertas = new ArrayList<>();
        if (lectura.getSaturacionOxigeno() < 90) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "SATURACION_CRITICA",
                    SeveridadAlerta.ALTA,
                    "Saturacion bajo 90%. Requiere revision inmediata.",
                    lectura.getSaturacionOxigeno() + "%"
            ));
        }
        if (lectura.getFrecuenciaCardiaca() < 45 || lectura.getFrecuenciaCardiaca() > 130) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "FRECUENCIA_CARDIACA_CRITICA",
                    SeveridadAlerta.ALTA,
                    "Frecuencia cardiaca fuera de rango critico.",
                    lectura.getFrecuenciaCardiaca() + " lpm"
            ));
        }
        if (lectura.getPresionSistolica() < 90 || lectura.getPresionSistolica() > 180) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "PRESION_ARTERIAL_CRITICA",
                    SeveridadAlerta.ALTA,
                    "Presion sistolica fuera de rango critico.",
                    lectura.getPresionSistolica() + "/" + lectura.getPresionDiastolica() + " mmHg"
            ));
        }
        if (lectura.getTemperatura().compareTo(BigDecimal.valueOf(38.5)) >= 0) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "FIEBRE_ALTA",
                    SeveridadAlerta.MEDIA,
                    "Temperatura superior o igual a 38.5 C.",
                    lectura.getTemperatura() + " C"
            ));
        }
        if (lectura.getFrecuenciaRespiratoria() < 8 || lectura.getFrecuenciaRespiratoria() > 30) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "FRECUENCIA_RESPIRATORIA_ANORMAL",
                    SeveridadAlerta.MEDIA,
                    "Frecuencia respiratoria fuera de rango esperado.",
                    lectura.getFrecuenciaRespiratoria() + " rpm"
            ));
        }
        return alertas;
    }

    private EventoClinicoMessage crearMensaje(
            Paciente paciente,
            SignoVital lectura,
            String tipo,
            SeveridadAlerta severidad,
            String mensaje,
            String valor
    ) {
        return new EventoClinicoMessage(
                paciente.getId(),
                tipo,
                "Dispositivo medico UCI - " + paciente.getHabitacion(),
                mensaje,
                severidad,
                valor,
                lectura.getFechaRegistro()
        );
    }

    private void publicar(EventoClinicoMessage alerta) {
        try {
            String payload = objectMapper.writeValueAsString(alerta);
            rabbitTemplate.convertAndSend(exchange, alertRoutingKey, payload, rabbitMessage -> {
                rabbitMessage.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                rabbitMessage.getMessageProperties().setContentEncoding("UTF-8");
                return rabbitMessage;
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("No se pudo convertir la alerta clinica a JSON.", exception);
        }
    }
}
