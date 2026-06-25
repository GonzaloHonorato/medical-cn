package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.ResumenSignosVitalesMessage;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SignoVital;
import com.medicalapp.medicalapp.repository.PacienteRepository;
import com.medicalapp.medicalapp.repository.SignoVitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResumenSignosVitalesProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResumenSignosVitalesProducerService.class);

    private final PacienteRepository pacienteRepository;
    private final SignoVitalRepository signoVitalRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String summaryRoutingKey;
    private final boolean schedulerEnabled;

    public ResumenSignosVitalesProducerService(
            PacienteRepository pacienteRepository,
            SignoVitalRepository signoVitalRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${medicalapp.rabbitmq.exchange}") String exchange,
            @Value("${medicalapp.rabbitmq.summary-routing-key}") String summaryRoutingKey,
            @Value("${medicalapp.summary.scheduler-enabled}") boolean schedulerEnabled
    ) {
        this.pacienteRepository = pacienteRepository;
        this.signoVitalRepository = signoVitalRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.summaryRoutingKey = summaryRoutingKey;
        this.schedulerEnabled = schedulerEnabled;
    }

    @Scheduled(fixedDelayString = "${medicalapp.summary.interval-ms}")
    public void publicarResumenProgramado() {
        if (!schedulerEnabled) {
            return;
        }
        ResumenSignosVitalesMessage message = construirResumen();
        publicar(message);
    }

    private ResumenSignosVitalesMessage construirResumen() {
        List<Paciente> pacientes = pacienteRepository.findByActivoTrueOrderByHabitacionAsc();
        List<ResumenSignosVitalesMessage.PacienteResumenSignos> resumenPacientes = pacientes.stream()
                .map(this::mapPaciente)
                .toList();
        int lecturasIncluidas = (int) resumenPacientes.stream()
                .filter(resumen -> resumen.ultimoSignoVital() != null)
                .count();
        return new ResumenSignosVitalesMessage(
                pacientes.size(),
                lecturasIncluidas,
                OffsetDateTime.now(),
                resumenPacientes
        );
    }

    private ResumenSignosVitalesMessage.PacienteResumenSignos mapPaciente(Paciente paciente) {
        Optional<SignoVital> ultimo = signoVitalRepository.findTopByPacienteIdOrderByFechaRegistroDesc(paciente.getId());
        return new ResumenSignosVitalesMessage.PacienteResumenSignos(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getHabitacion(),
                paciente.getEstado(),
                ultimo.map(this::mapSignoVital).orElse(null)
        );
    }

    private ResumenSignosVitalesMessage.UltimoSignoVital mapSignoVital(SignoVital signoVital) {
        return new ResumenSignosVitalesMessage.UltimoSignoVital(
                signoVital.getId(),
                signoVital.getFrecuenciaCardiaca(),
                signoVital.getPresionSistolica(),
                signoVital.getPresionDiastolica(),
                signoVital.getSaturacionOxigeno(),
                signoVital.getTemperatura(),
                signoVital.getFrecuenciaRespiratoria(),
                signoVital.getFechaRegistro()
        );
    }

    private void publicar(ResumenSignosVitalesMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(exchange, summaryRoutingKey, payload, rabbitMessage -> {
                rabbitMessage.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                rabbitMessage.getMessageProperties().setContentEncoding("UTF-8");
                return rabbitMessage;
            });
            LOGGER.info(
                    "Productor 2 publico resumen de signos vitales. pacientesActivos={}, lecturasIncluidas={}",
                    message.pacientesActivos(),
                    message.lecturasIncluidas()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("No se pudo convertir el resumen de signos vitales a JSON.", exception);
        }
    }
}
