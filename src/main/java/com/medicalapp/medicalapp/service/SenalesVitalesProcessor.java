package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.dto.SenalVitalMessage;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SignoVital;
import com.medicalapp.medicalapp.repository.PacienteRepository;
import com.medicalapp.medicalapp.repository.SignoVitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Microservicio de procesamiento de señales (Tarea 2).
 * Consume el topico "senales_vitales", persiste la lectura para el dashboard,
 * detecta valores anomalos y, cuando los hay, los publica en el topico "alertas".
 */
@Service
public class SenalesVitalesProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SenalesVitalesProcessor.class);

    private final ObjectMapper objectMapper;
    private final PacienteRepository pacienteRepository;
    private final SignoVitalRepository signoVitalRepository;
    private final DetectorAnomalias detectorAnomalias;
    private final AlertaKafkaProducer alertaKafkaProducer;

    public SenalesVitalesProcessor(
            ObjectMapper objectMapper,
            PacienteRepository pacienteRepository,
            SignoVitalRepository signoVitalRepository,
            DetectorAnomalias detectorAnomalias,
            AlertaKafkaProducer alertaKafkaProducer
    ) {
        this.objectMapper = objectMapper;
        this.pacienteRepository = pacienteRepository;
        this.signoVitalRepository = signoVitalRepository;
        this.detectorAnomalias = detectorAnomalias;
        this.alertaKafkaProducer = alertaKafkaProducer;
    }

    @KafkaListener(
            topics = "${medicalapp.kafka.topic.senales-vitales}",
            groupId = "${medicalapp.kafka.group.procesador:procesador}"
    )
    @Transactional
    public void procesar(String payload) {
        SenalVitalMessage lectura = leer(payload);
        Paciente paciente = pacienteRepository.findById(lectura.pacienteId())
                .filter(Paciente::getActivo)
                .orElse(null);
        if (paciente == null) {
            LOGGER.warn("Señal vital ignorada: paciente {} no encontrado o inactivo.", lectura.pacienteId());
            return;
        }

        SignoVital guardada = signoVitalRepository.save(new SignoVital(
                null,
                paciente,
                lectura.frecuenciaCardiaca(),
                lectura.presionSistolica(),
                lectura.presionDiastolica(),
                lectura.saturacionOxigeno(),
                lectura.temperatura(),
                lectura.frecuenciaRespiratoria(),
                lectura.fechaLectura() == null ? OffsetDateTime.now() : lectura.fechaLectura()
        ));

        List<EventoClinicoMessage> alertas = detectorAnomalias.detectar(paciente, guardada);
        alertas.forEach(alertaKafkaProducer::enviar);
        if (!alertas.isEmpty()) {
            LOGGER.info(
                    "Procesador detecto {} anomalia(s) para paciente {} y las publico en '{}'",
                    alertas.size(),
                    paciente.getId(),
                    alertaKafkaProducer.topic()
            );
        }
    }

    private SenalVitalMessage leer(String payload) {
        try {
            return objectMapper.readValue(payload, SenalVitalMessage.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Mensaje invalido en el topico de señales vitales.", exception);
        }
    }
}
